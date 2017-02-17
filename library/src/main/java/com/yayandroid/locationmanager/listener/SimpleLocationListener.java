package com.yayandroid.locationmanager.listener;

import android.location.Location;
import android.os.Bundle;

public abstract class SimpleLocationListener implements LocationListener {

    @Override
    public abstract void onLocationChanged(Location location);

    @Override
    public abstract void onLocationFailed(int type);

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
