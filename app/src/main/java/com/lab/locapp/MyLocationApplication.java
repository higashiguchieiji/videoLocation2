package com.lab.locapp;

import android.app.Application;
import io.realm.Realm;
import io.realm.RealmConfiguration;

public class MyLocationApplication extends Application {
    @Override
    public void onCreate(){
        super.onCreate();
        Realm.init(this);
        RealmConfiguration realmConfig = new RealmConfiguration.Builder().build();
        Realm.deleteRealm(realmConfig);
        Realm realm = Realm.getInstance(realmConfig);
        Realm.setDefaultConfiguration(realmConfig);
    }
}