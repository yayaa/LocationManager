package com.yayandroid.locationmanager.base;

import android.app.Service;
import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.CallSuper;

import com.yayandroid.locationmanager.LocationManager;
import com.yayandroid.locationmanager.configuration.LocationConfiguration;
import com.yayandroid.locationmanager.constants.ProcessType;
import com.yayandroid.locationmanager.listener.LocationListener;

public abstract class LocationBaseService extends Service implements LocationListener {

    private LocationManager locationManager;

    public abstract LocationConfiguration getLocationConfiguration();

    @CallSuper
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        locationManager = new LocationManager.Builder(getApplicationContext())
              .configuration(getLocationConfiguration())
              .notify(this)
              .build();
        return super.onStartCommand(intent, flags, startId);
    }

    protected LocationManager getLocationManager() {
        return locationManager;
    }

    protected void getLocation() {
        if (locationManager != null) {
            locationManager.get();
        } else {
            throw new IllegalStateException("locationManager is null. "
                  + "Make sure you call super.onStartCommand before attempting to getLocation");
        }
    }

    @Override
    public void onProcessTypeChanged(@ProcessType int processType) {
        // override if needed
    }

    @Override
    public void onPermissionGranted(boolean alreadyHadPermission) {
        // override if needed
    }

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {
        // override if needed
    }

    @Override
    public void onProviderEnabled(String provider) {
        // override if needed
    }

    @Override
    public void onProviderDisabled(String provider) {
        // override if needed
    }
}
