package com.fbarco.iluminacion;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.Intent;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;



import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.MulticastSocket;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class MainActivity extends AppCompatActivity {
    private static final String TAG = "APITEST";
    private static final int MSG_SHOWLOG = 0;
    private static final int MSG_FOUND_DEVICE = 1;
    private static final int MSG_DISCOVER_FINISH = 2;
    private static final int MSG_STOP_SEARCH = 3;

    private static final String UDP_HOST = "239.255.255.250";
    private static final int UDP_PORT = 1982;
    private static final String message = "M-SEARCH * HTTP/1.1\r\n" +
            "HOST:239.255.255.250:1982\r\n" +
            "MAN:\"ssdp:discover\"\r\n" +
            "ST:wifi_bulb\r\n";
    private DatagramSocket mDSocket;
    private boolean mSeraching = true;
    private ListView mListView;
    private MyAdapter mAdapter;
    List<HashMap<String, String>> mDeviceList = new ArrayList<HashMap<String, String>>();
    private TextView mTextView;
    private Button mBtnSearch;
    private Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what) {
                case MSG_FOUND_DEVICE:
                    mAdapter.notifyDataSetChanged();
                    break;
                case MSG_SHOWLOG:
                    Toast.makeText(MainActivity.this, "" + msg.obj.toString(), Toast.LENGTH_SHORT).show();
                    break;
                case MSG_STOP_SEARCH:
                    mSearchThread.interrupt();
                    mAdapter.notifyDataSetChanged();
                    mSeraching = false;
                    break;
                case MSG_DISCOVER_FINISH:
                    mAdapter.notifyDataSetChanged();
                    break;
            }
        }
    };
    private WifiManager.MulticastLock multicastLock;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        WifiManager wm = (WifiManager)getApplicationContext().getSystemService(Context.WIFI_SERVICE);
        multicastLock = wm.createMulticastLock("test");
        multicastLock.acquire();


        mBtnSearch = findViewById(R.id.busqueda);
        mBtnSearch.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
/*                Intent intent = new Intent(MainActivity.this, ControlActivity.class);
                intent.putExtra("ip", "172.16.10.3");
                intent.putExtra("power","off");
                intent.putExtra("port", "55443");
                intent.putExtra("name","nameMain");
                intent.putExtra("mbright", "99");
                startActivity(intent);*/
                searchDevice();
            }

        });
        mListView = findViewById(R.id.lista);
        //mAdapter = new MyAdapter(this);
        mAdapter = new MyAdapter(this, R.layout.item, mDeviceList);

        mListView.setAdapter(mAdapter);
        mListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                HashMap<String, String> bulbInfo = mDeviceList.get(position);
                Intent intent = new Intent(MainActivity.this, ControlActivity.class);
                String ipinfo = bulbInfo.get("Location").split("//")[1];
                String nameMain = bulbInfo.get("name");
                String mbright = bulbInfo.get("bright");
                String power = bulbInfo.get("power");
                //Log.d("power","dddddddddddddddd " + power);
                String ip = ipinfo.split(":")[0];
                String port = ipinfo.split(":")[1];

                intent.putExtra("bulbinfo", bulbInfo);
                intent.putExtra("ip", ip);
                intent.putExtra("power",power);
                intent.putExtra("port", port);
                intent.putExtra("name",nameMain);
                intent.putExtra("mbright", mbright);
                startActivity(intent);
            }
        });
    }
    private Thread mSearchThread = null;
    private void searchDevice() {

        mDeviceList.clear();
        mAdapter.notifyDataSetChanged();
        mSeraching = true;
        mSearchThread = new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    mDSocket = new DatagramSocket();
                    DatagramPacket dpSend = new DatagramPacket(message.getBytes(),
                            message.getBytes().length, InetAddress.getByName(UDP_HOST),
                            UDP_PORT);
                    mDSocket.send(dpSend);
                    mHandler.sendEmptyMessageDelayed(MSG_STOP_SEARCH,2000);
                    while (mSeraching) {
                        byte[] buf = new byte[1024];
                        DatagramPacket dpRecv = new DatagramPacket(buf, buf.length);
                        mDSocket.receive(dpRecv);
                        byte[] bytes = dpRecv.getData();
                        StringBuffer buffer = new StringBuffer();
                        for (int i = 0; i < dpRecv.getLength(); i++) {
                            // parse /r
                            if (bytes[i] == 13) {
                                continue;
                            }
                            buffer.append((char) bytes[i]);
                        }
                        Log.d("socket", "got message:" + buffer.toString());
                        if (!buffer.toString().contains("yeelight")) {
                            mHandler.obtainMessage(MSG_SHOWLOG, "Yeelight_1").sendToTarget();
                            return;
                        }
                        String[] infos = buffer.toString().split("\n");
                        HashMap<String, String> bulbInfo = new HashMap<String, String>();
                        for (String str : infos) {
                            int index = str.indexOf(":");
                            if (index == -1) {
                                continue;
                            }
                            String title = str.substring(0, index);
                            String value = str.substring(index + 1);
                            bulbInfo.put(title, value);
                        }
                        if (!hasAdd(bulbInfo)){
                            mDeviceList.add(bulbInfo);
                        }

                    }
                    mHandler.sendEmptyMessage(MSG_DISCOVER_FINISH);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
        mSearchThread.start();

    }

    private boolean mNotify = true;
    @Override
    protected void onResume() {
        super.onResume();
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    //DatagramSocket socket = new DatagramSocket(UDP_PORT);
                    InetAddress group = InetAddress.getByName(UDP_HOST);
                    MulticastSocket socket = new MulticastSocket(UDP_PORT);
                    socket.setLoopbackMode(true);
                    socket.joinGroup(group);
                    Log.d(TAG, "join success");
                    mNotify = true;
                    while (mNotify){
                        byte[] buf = new byte[1024];
                        DatagramPacket receiveDp = new DatagramPacket(buf,buf.length);
                        Log.d(TAG, "waiting device....");
                        socket.receive(receiveDp);
                        byte[] bytes = receiveDp.getData();
                        StringBuffer buffer = new StringBuffer();
                        for (int i = 0; i < receiveDp.getLength(); i++) {
                            // parse /r
                            if (bytes[i] == 13) {
                                continue;
                            }
                            buffer.append((char) bytes[i]);
                        }
                        if (!buffer.toString().contains("yeelight")){
                            Log.d(TAG,"Listener receive msg:" + buffer.toString()+" but not a response");
                            return;
                        }
                        String[] infos = buffer.toString().split("\n");
                        HashMap<String, String> bulbInfo = new HashMap<String, String>();
                        for (String str : infos) {
                            int index = str.indexOf(":");
                            if (index == -1) {
                                continue;
                            }
                            String title = str.substring(0, index);
                            String value = str.substring(index + 1);
                            Log.d(TAG, "title = " + title + " value = " + value);
                            bulbInfo.put(title, value);
                        }
                        if (!hasAdd(bulbInfo)){
                            mDeviceList.add(bulbInfo);
                        }
                        mHandler.sendEmptyMessage(MSG_FOUND_DEVICE);
                        Log.d(TAG, "get message:" + buffer.toString());
                    }
                }catch (Exception e){
                    e.printStackTrace();
                }

            }
        }).start();
    }

    @Override
    protected void onPause() {
        super.onPause();
        mNotify = false;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        multicastLock.release();
    }

    /*    private class MyAdapter extends BaseAdapter {

            private LayoutInflater mLayoutInflater;
            private int mLayoutResource;

            public MyAdapter(Context context) {
                mLayoutInflater = LayoutInflater.from(context);
                mLayoutResource = android.R.layout.simple_list_item_2;
            }

            @Override
            public int getCount() {
                return mDeviceList.size();
            }

            @Override
            public Object getItem(int position) {
                return mDeviceList.get(position);
            }

            @Override
            public long getItemId(int position) {
                return 0;
            }

            @Override
            public View getView(int position, View convertView, ViewGroup parent) {
                View view;
                HashMap<String, String> data = (HashMap<String, String>) getItem(position);
                if (convertView == null) {
                    view = mLayoutInflater.inflate(mLayoutResource, parent, false);
                } else {
                    view = convertView;
                }
                TextView textView = (TextView) view.findViewById(android.R.id.text1);
                textView.setText("Type = "+data.get("model") + "  Name = " + data.get("name") );

                Log.d(TAG, "name = " + textView.getText().toString());
                TextView textSub = (TextView) view.findViewById(android.R.id.text2);
                // TextView nuevo = (TextView) view.findViewById(android.R.id.text3);
                textSub.setText("location = " + data.get("Location"));
                return view;
            }
        }*/
    private boolean hasAdd(HashMap<String,String> bulbinfo){
        for (HashMap<String,String> info : mDeviceList){
            Log.d(TAG, "location params = " + bulbinfo.get("Location"));
            if (info.get("Location").equals(bulbinfo.get("Location"))){
                return true;
            }
        }
        return false;
    }
}
