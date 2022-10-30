package com.fbarco.iluminacion;

import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.widget.Button;
import android.widget.SeekBar;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.Socket;

public class ControlActivity extends AppCompatActivity implements IWidgetActivity{

    private String TAG = "Control";

    private static final int MSG_CONNECT_SUCCESS = 0;
    private static final int MSG_CONNECT_FAILURE = 1;
    private static final String CMD_TOGGLE = "{\"id\":%id,\"method\":\"toggle\",\"params\":[]}\r\n" ;
    private static final String CMD_ON = "{\"id\":%id,\"method\":\"set_power\",\"params\":[\"on\",\"smooth\",500]}\r\n" ;
    private static final String CMD_OFF = "{\"id\":%id,\"method\":\"set_power\",\"params\":[\"off\",\"smooth\",500]}\r\n" ;
    private static final String CMD_CT = "{\"id\":%id,\"method\":\"set_ct_abx\",\"params\":[%value, \"smooth\", 500]}\r\n";
    private static final String CMD_HSV = "{\"id\":%id,\"method\":\"set_hsv\",\"params\":[%value, 100, \"smooth\", 200]}\r\n";
    private static final String CMD_BRIGHTNESS = "{\"id\":%id,\"method\":\"set_bright\",\"params\":[%value, \"smooth\", 200]}\r\n";
    private static final String CMD_BRIGHTNESS_SCENE = "{\"id\":%id,\"method\":\"set_bright\",\"params\":[%value, \"smooth\", 500]}\r\n";
    private static final String CMD_COLOR_SCENE = "{\"id\":%id,\"method\":\"set_scene\",\"params\":[\"cf\",1,0,\"100,1,%color,1\"]}\r\n";
    private static final String CMD_GET_PROP = "{\"id\":%id,\"method\":\"get_prop\",\"params\":[\"name\",\"power\",\"bright\",\"delayoff\"]}\r\n";
    private static final String CMD_ADD_CRON = "{\"id\":%id,\"method\":\"cron_add\",\"params\":[%type, %value]}\r\n";

    private CircularSlider CS;


    private int mCmdId;
    private int valuetime=15;
    private String value;
    private Socket mSocket;
    private String mBulbIP;
    private int mBulbPort;
    private String mBright;
    private String mPower;
    private float brillo;
    private ProgressDialog mProgressDialog;
    private SeekBar mBrightness;
    private SeekBar mCT;
    private SeekBar mColor;
    private Button mBtnOn;
    private Button mBtnOff;
    private Button mBtnMusic;
    private Button mBtnname;
    private Button mBtnsleep;
    private Spinner spinner;
    private TextView nameText;
    private String mTitle;


    private BufferedOutputStream mBos;
    private BufferedReader mReader;
    private Handler mHandler = new Handler(){
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what){
                case MSG_CONNECT_FAILURE:
                    mProgressDialog.dismiss();
                    break;
                case MSG_CONNECT_SUCCESS:
                    mProgressDialog.dismiss();
                    break;
            }
        }
    };
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_control);
        CS = findViewById(R.id.circle);
        CS.setOnSliderRangeMovedListener(this);
        CS.setmAngle(54);


        mBulbIP = getIntent().getStringExtra("ip");
        mTitle = getIntent().getStringExtra("name");
        setTitle(mTitle + "  -  " + mBulbIP);
        mBulbPort = Integer.parseInt(getIntent().getStringExtra("port"));
        mBright = getIntent().getStringExtra("mbright");
        mPower = getIntent().getStringExtra("power");
        //Log.d("power","hhhhhhhhhhhhhhhhhhhhhhhhhh"+mPower.trim()+"b");
        brillo = Float.parseFloat(mBright);
        Log.d("power","sssssssssssssssssssssss "+brillo);
        CS.setmAngle((int)brillo);
        if(mPower.trim().equals("on")){CS.setmIsActivate(true);Log.d("power","onnnnnnnnnnnnnnn");}
        if(mPower.trim().equals("off")){CS.setmIsActivate(false);Log.d("power","offffffffffffff ");}
        mProgressDialog = new ProgressDialog(this);
        mProgressDialog.setMessage("Connecting...");
        mProgressDialog.setCancelable(false);
        mProgressDialog.show();
        Toast.makeText(this,mBulbIP + " " + mBulbPort + " " + mBright + " " + mPower,Toast.LENGTH_LONG).show();


        connect();
    }

    private boolean cmd_run = true;
    private void connect(){
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    cmd_run = true;
                    mSocket = new Socket(mBulbIP, mBulbPort);
                    mSocket.setKeepAlive(true);
                    mBos= new BufferedOutputStream(mSocket.getOutputStream());
                    mHandler.sendEmptyMessage(MSG_CONNECT_SUCCESS);
                    mReader = new BufferedReader(new InputStreamReader(mSocket.getInputStream()));
                    while (cmd_run){
                        try {
                            value = mReader.readLine();
                            //write(parseGetProp());
                            //nameText.setText(value+"\n"+mBulbIP);
                            Log.d(TAG, "value = "+value);
                        }catch (Exception e){
                            Log.e(TAG, "no inicio conexión");
                        }

                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    mHandler.sendEmptyMessage(MSG_CONNECT_FAILURE);
                }
            }
        }).start();
    }


    @Override
    protected void onDestroy() {
        super.onDestroy();
        try{
            cmd_run = false;
            if (mSocket!=null)
                mSocket.close();
        }catch (Exception e){

        }

    }

    private void write(String cmd){

        Thread thread = new Thread(new Runnable() {

            @Override
            public void run() {
                try  {
                    if (mBos != null && mSocket.isConnected()){
                        try {
                            mBos.write(cmd.getBytes());
                            mBos.flush();
                        }catch (Exception e){
                            e.printStackTrace();
                        }
                    } else {
                        Log.d(TAG,"mBos = null or mSocket is closed");
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });

        thread.start();
    }

    private String parseGetProp(){
        String cmd;
        cmd = CMD_GET_PROP.replace("%id", String.valueOf(++mCmdId));
        return cmd;
    }

    private String parseSwitch(boolean on){
        String cmd;
        if (on){
            cmd = CMD_ON.replace("%id", String.valueOf(++mCmdId));
        }else {
            cmd = CMD_OFF.replace("%id", String.valueOf(++mCmdId));
        }
        return cmd;
    }

    private String parseBrightnessCmd(int brightness){
        return CMD_BRIGHTNESS.replace("%id",String.valueOf(++mCmdId)).replace("%value",String.valueOf(brightness));
    }

    @Override
    public void valuechange(int angle) {

    }

    @Override
    public void valueStopchange(int angle) {
        write(parseBrightnessCmd(angle));
        Log.d(TAG, "Angle : " + angle);
    }

    @Override
    public void ClickOn() {
        Toast.makeText(this,"ON",Toast.LENGTH_LONG).show();
        write(parseSwitch(true));
    }

    @Override
    public void ClickOff() {
        Toast.makeText(this,"OFF",Toast.LENGTH_LONG).show();
        write(parseSwitch(false));
    }
}
