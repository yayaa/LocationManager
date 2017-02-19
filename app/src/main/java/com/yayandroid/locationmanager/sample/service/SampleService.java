package com.yayandroid.locationmanager.sample.service;

import android.content.Intent;
import android.location.Location;
import android.os.IBinder;
import android.support.annotation.Nullable;

import com.yayandroid.locationmanager.base.LocationBaseService;
import com.yayandroid.locationmanager.configuration.Configurations;
import com.yayandroid.locationmanager.configuration.LocationConfiguration;

public class SampleService extends LocationBaseService {

    public static final String ACTION_LOCATION_CHANGED = "com.yayandroid.locationmanager.sample.service.LOCATION_CHANGED";
    public static final String ACTION_LOCATION_FAILED = "com.yayandroid.locationmanager.sample.service.LOCATION_FAILED";

    public static final String EXTRA_LOCATION = "ExtraLocationField";
    public static final String EXTRA_FAIL_TYPE = "ExtraFailTypeField";

    private boolean isLocationRequested = false;

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public LocationConfiguration getLocationConfiguration() {
        return Configurations.silenceConfiguration(false);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        // calling super is required when extending from LocationBaseService
        super.onStartCommand(intent, flags, startId);

        if(!isLocationRequested) {
            isLocationRequested = true;
            getLocation();
        }

        // Return type is depends on your requirements
        return START_NOT_STICKY;
    }

    @Override
    public void onLocationChanged(Location location) {
        Intent intent = new Intent(ACTION_LOCATION_CHANGED);
        intent.putExtra(EXTRA_LOCATION, location);
        sendBroadcast(intent);

        stopSelf();
    }

    @Override
    public void onLocationFailed(int type) {
        Intent intent = new Intent(ACTION_LOCATION_FAILED);
        intent.putExtra(EXTRA_FAIL_TYPE, type);
        sendBroadcast(intent);

        stopSelf();
    }
}
