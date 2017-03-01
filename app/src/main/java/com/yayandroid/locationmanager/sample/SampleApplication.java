package com.yayandroid.locationmanager.sample;

import android.app.Application;

import com.yayandroid.locationmanager.LocationManager;

public class SampleApplication extends Application {

    @Override
    public void onCreate() {
        super.onCreate();
        LocationManager.enableLog(true);
    }
}
