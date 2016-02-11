package com.yayandroid.locationmanager;

import android.location.Location;
import android.os.Bundle;

/**
 * Created by Yahya Bayramoglu on 09/02/16.
 */
public abstract class LocationReceiver {

    /**
     * This method will be invoked whenever new location update received
     */
    public abstract void onLocationChanged(Location location);

    /**
     * When it is not possible to receive location, such as no active provider or no permission etc.
     */
    public abstract void onLocationFailed();

    /**
     * This method will be invoked if only you use android.location.LocationManager
     * with GPS or Network Providers to receive location
     */
    public void onStatusChanged(String provider, int status, Bundle extras) {
    }

    /**
     * This method will be invoked if only you use android.location.LocationManager
     * with GPS or Network Providers to receive location
     */
    public void onProviderEnabled(String provider) {
    }

    /**
     * This method will be invoked if only you use android.location.LocationManager
     * with GPS or Network Providers to receive location
     */
    public void onProviderDisabled(String provider) {
    }

}