package com.yayandroid.locationmanager.constants;

import android.Manifest;

import com.google.android.gms.location.LocationRequest;

/**
 * Created by Yahya Bayramoglu on 10/02/16.
 */
public class Default {

    private static final int SECOND = 1000;
    private static final int MINUTE = 60 * SECOND;

    public static final int WAIT_PERIOD = 20 * SECOND;
    public static final int TIME_PERIOD = 5 * MINUTE;

    public static final int LOCATION_INTERVAL = 5 * MINUTE;
    public static final int LOCATION_FASTEST_INTERVAL = MINUTE;
    public static final int LOCATION_PRIORITY = LocationRequest.PRIORITY_BALANCED_POWER_ACCURACY;

    public static final float MIN_ACCURACY = 5.0f;

    public static final boolean KEEP_TRACKING = false;
    public static final boolean ASK_FOR_GP_SERVICES = false;
    public static final boolean ASK_FOR_SETTINGS_API = true;
    public static final boolean FAIL_ON_CONNECTION_SUSPENDED = true;
    public static final boolean FAIL_ON_SETTINGS_API_SUSPENDED = false;

    public static final String[] LOCATION_PERMISSIONS = new String[]{
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.ACCESS_COARSE_LOCATION
    };

}