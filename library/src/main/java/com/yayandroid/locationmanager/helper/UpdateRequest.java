package com.yayandroid.locationmanager.helper;

import android.location.LocationListener;
import android.location.LocationManager;

public class UpdateRequest {

    private final LocationManager locationManager;
    private final LocationListener locationListener;

    private String provider;
    private long minTime;
    private float minDistance;

    public UpdateRequest(LocationManager locationManager, LocationListener locationListener) {
        this.locationManager = locationManager;
        this.locationListener = locationListener;
    }

    public boolean isRequiredImmediately() {
        return minTime == 0;
    }

    public void run(String provider, long minTime, float minDistance) {
        this.provider = provider;
        this.minTime = minTime;
        this.minDistance = minDistance;
        run();
    }

    @SuppressWarnings("ResourceType")
    public void run() {
        if(StringUtils.isNotEmpty(provider)) {
            locationManager.requestLocationUpdates(provider, minTime, minDistance, locationListener);
        }
    }

    @SuppressWarnings("ResourceType")
    public void release() {
        locationManager.removeUpdates(locationListener);
    }

}
