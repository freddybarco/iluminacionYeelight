package com.fbarco.iluminacion;

import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.HashMap;
import java.util.List;

public class MyAdapter extends BaseAdapter {
    Context context;
    int layout;
    List<String> names;
    List<HashMap<String, String>> mDeviceList;


    public MyAdapter(Context context,int layout,List<HashMap<String, String>> mDeviceList) {
        this.context =context;
        this.layout = layout;
        this.mDeviceList = mDeviceList;

    }

    @Override
    public int getCount() {
        return this.mDeviceList.size();
    }

    @Override
    public Object getItem(int position) {
        return this.mDeviceList.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        View v = convertView;
        HashMap<String, String> data = (HashMap<String, String>) getItem(position);

        LayoutInflater layoutInflater = LayoutInflater.from(this.context);

        v = layoutInflater.inflate(R.layout.item,null);

        String currentType = "Type = "+data.get("model") ;
        String currentName = "Name = " + data.get("name");
        String currentLocation = "location = " + data.get("Location") ;


        TextView type = v.findViewById(R.id.type);
        TextView name = v.findViewById(R.id.name);
        TextView location = v.findViewById(R.id.location);
        type.setTextColor(Color.WHITE);
        name.setTextColor(Color.WHITE);
        location.setTextColor(Color.WHITE);
        ImageView imagen = v.findViewById(R.id.foto);
        type.setText(currentType);
        name.setText(currentName);
        location.setText(currentLocation);

        imagen.setImageResource(R.drawable.ic_foco);
        return v;
    }
}
