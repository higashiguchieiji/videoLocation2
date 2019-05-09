package com.lab.locapp;

import android.location.Location;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import io.realm.OrderedRealmCollection;
import io.realm.RealmBaseAdapter;
import io.realm.RealmResults;

public class LocationAdapter extends RealmBaseAdapter<LocationApp> {

    private  static class ViewHolder{
        TextView ido;
        TextView keido;
        TextView koudo;
    }

    public LocationAdapter(@Nullable RealmResults<LocationApp> data) {
        super(data);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder viewHolder;

        if(convertView == null){
            convertView = LayoutInflater.from(parent.getContext()).inflate(android.R.layout.simple_list_item_2, parent, false);
            viewHolder = new ViewHolder();
            viewHolder.ido = (TextView)convertView.findViewById(android.R.id.text1);
            viewHolder.keido = (TextView)convertView.findViewById(android.R.id.text2);
//            viewHolder.keido = (TextView)convertView.findViewById(android.R.id.text3);
            convertView.setTag(viewHolder);
        }
        else{
            viewHolder = (ViewHolder)convertView.getTag();
        }
        LocationApp locationApp = adapterData.get(position);
        viewHolder.ido.setText(locationApp.getIdo());
        viewHolder.keido.setText(locationApp.getKeido());
//        viewHolder.koudo.setText(locationApp.getKoudo());

        return convertView;
    }
}
