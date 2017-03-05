package com.yayandroid.locationmanager.helper;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface.OnCancelListener;
import android.location.Location;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.support.annotation.Nullable;

import com.google.android.gms.common.GoogleApiAvailability;

import java.util.Date;

public final class LocationUtils {

    private LocationUtils() {
        // No instance
    }

    public static int isGooglePlayServicesAvailable(Context context) {
        if (context == null) return -1;
        return GoogleApiAvailability.getInstance().isGooglePlayServicesAvailable(context);
    }

    @Nullable public static Dialog getGooglePlayServicesErrorDialog(Activity activity, int gpServicesAvailability,
          int requestCode, OnCancelListener onCancelListener) {
        if (activity == null) return null;
        return GoogleApiAvailability.getInstance()
              .getErrorDialog(activity, gpServicesAvailability, requestCode, onCancelListener);
    }

    public static boolean isNetworkAvailable(Context context) {
        if (context == null) return false;
        ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = cm.getActiveNetworkInfo();
        return activeNetworkInfo != null;
    }

    public static boolean isUsable(Location location, long acceptableTimePeriod, float acceptableAccuracy) {
        if (location == null) return false;

        float givenAccuracy = location.getAccuracy();
        long givenTime = location.getTime();
        long minAcceptableTime = new Date().getTime() - acceptableTimePeriod;

        return minAcceptableTime <= givenTime && acceptableAccuracy >= givenAccuracy;
    }
}
