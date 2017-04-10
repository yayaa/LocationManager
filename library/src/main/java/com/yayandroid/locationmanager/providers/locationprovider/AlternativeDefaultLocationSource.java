package com.yayandroid.locationmanager.providers.locationprovider;


import android.content.Context;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;

import com.yayandroid.locationmanager.constants.ProviderType;

import java.util.Date;

class AlternativeDefaultLocationSource implements LocationListener {

    private LocationManager locationManager;
    private String locationProvider;

    private LocationUpdateListener locationUpdateListener;

    private Location lastLocation;
    private long lastTime;

    private boolean isRunning;

    interface LocationUpdateListener {

        void onUpdateLocation(Location oldLocation, long oldTime, Location newLocation, long newTime);

        void onStatusChanged(String provider, int status, Bundle extras);

        void onProviderEnabled(String provider);

        void onProviderDisabled(String provider);
    }

    AlternativeDefaultLocationSource(Context context, @ProviderType int providerType){

        locationManager = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);

        if(providerType == ProviderType.GPS){
            locationProvider = LocationManager.GPS_PROVIDER;
        } else if(providerType == ProviderType.NETWORK){
            locationProvider = LocationManager.NETWORK_PROVIDER;
        } else {
            throw new IllegalStateException("Choose correct provider");
        }
    }

    @SuppressWarnings("ResourceType")
    void startLocationListener(LocationUpdateListener locationUpdateListener, long timeInterval, float distanceInterval) {
        if(isRunning)
            return;

        isRunning = true;
        locationManager.requestLocationUpdates(locationProvider, timeInterval, distanceInterval, this);

        lastLocation = null;
        lastTime = 0;
        this.locationUpdateListener = locationUpdateListener;

    }

    @SuppressWarnings("ResourceType")
    void stopLocationListener() {
        if(isRunning){
            locationManager.removeUpdates(this);

            isRunning = false;
            locationUpdateListener = null;
        }
    }

    boolean isProviderEnabled() {
        return locationManager.isProviderEnabled(locationProvider);
    }

    @SuppressWarnings("ResourceType")
    Location getLastKnownLocation(){
        return locationManager.getLastKnownLocation(locationProvider);
    }


    boolean hasSufficientLastKnownLocation(Location location, long acceptableTimePeriod, float acceptableAccuracy){
        if(location == null){
            return false;
        }
        float givenAccuracy = location.getAccuracy();
        long givenTime = location.getTime();
        long minAcceptableTime = new Date().getTime() - acceptableTimePeriod;

        return minAcceptableTime <= givenTime && acceptableAccuracy >= givenAccuracy;
    }


    @Override
    public void onLocationChanged(Location location) {

        long currentTime = System.currentTimeMillis();
        if(locationUpdateListener != null){
            locationUpdateListener.onUpdateLocation(lastLocation, lastTime, location, currentTime);
        }
        lastLocation = location;
        lastTime = currentTime;
    }

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {
        if(locationUpdateListener != null){
            locationUpdateListener.onStatusChanged(provider, status, extras);
        }

    }

    @Override
    public void onProviderEnabled(String provider) {
        if(locationUpdateListener != null){
            locationUpdateListener.onProviderEnabled(provider);
        }
    }

    @Override
    public void onProviderDisabled(String provider) {
        if(locationUpdateListener != null){
            locationUpdateListener.onProviderDisabled(provider);
        }
    }
}
