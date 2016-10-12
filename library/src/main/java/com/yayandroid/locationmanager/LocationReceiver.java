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
     * It will pass an integer value from {@link com.yayandroid.locationmanager.constants.FailType}
     * which will help you to determine how did it fail to receive location
     */
    public abstract void onLocationFailed(int type);

    /**
     * This method will be invoked when user grants for location permissions,
     * or when you ask for it but the application already had that granted.
     * You can determine if permission is just granted or
     * did the application already have it by checking boolean input of this method.
     */
    public void onPermissionGranted(boolean alreadyHadPermission) {
    }

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