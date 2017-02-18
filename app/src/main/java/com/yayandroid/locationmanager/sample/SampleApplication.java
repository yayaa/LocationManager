package com.yayandroid.locationmanager.sample;

import android.app.Application;

import com.yayandroid.locationmanager.LocationManager;
import com.yayandroid.locationmanager.constants.LogType;

public class SampleApplication extends Application {

    @Override
    public void onCreate() {
        super.onCreate();
        LocationManager.setLogType(LogType.GENERAL);
    }
}
