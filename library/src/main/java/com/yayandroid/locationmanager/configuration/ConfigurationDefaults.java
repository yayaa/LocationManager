package com.yayandroid.locationmanager.configuration;

import android.Manifest;

import com.google.android.gms.location.LocationRequest;

class ConfigurationDefaults {

    private static final int SECOND = 1000;
    private static final int MINUTE = 60 * SECOND;

    static final int WAIT_PERIOD = 20 * SECOND;
    static final int TIME_PERIOD = 5 * MINUTE;

    static final int LOCATION_INTERVAL = 5 * MINUTE;
    static final int LOCATION_FASTEST_INTERVAL = MINUTE;
    static final int LOCATION_PRIORITY = LocationRequest.PRIORITY_BALANCED_POWER_ACCURACY;

    static final float MIN_ACCURACY = 5.0f;

    static final boolean KEEP_TRACKING = false;
    static final boolean ASK_FOR_GP_SERVICES = false;
    static final boolean ASK_FOR_SETTINGS_API = true;
    static final boolean FAIL_ON_CONNECTION_SUSPENDED = true;
    static final boolean FAIL_ON_SETTINGS_API_SUSPENDED = false;

    static final String EMPTY_STRING = "";
    static final String[] LOCATION_PERMISSIONS = new String[]{
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.ACCESS_COARSE_LOCATION
    };

}