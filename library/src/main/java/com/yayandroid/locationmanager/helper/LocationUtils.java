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
import com.yayandroid.locationmanager.configuration.LocationConfiguration;

import java.util.Date;
import java.util.List;

public class LocationUtils {

    public static int isGooglePlayServicesAvailable(Context context) {
        if (context == null) return -1;
        return GoogleApiAvailability.getInstance().isGooglePlayServicesAvailable(context);
    }

    public static @Nullable Dialog getGooglePlayServicesErrorDialog(Context context, int gpServicesAvailability,
          int requestCode, OnCancelListener onCancelListener) {
        if (context == null || !(context instanceof Activity)) return null;
        return GoogleApiAvailability.getInstance().getErrorDialog((Activity) context, gpServicesAvailability,
              requestCode, onCancelListener);
    }

    public static boolean isNetworkAvailable(Context context) {
        if (context == null) return false;
        ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = cm.getActiveNetworkInfo();
        return activeNetworkInfo != null;
    }

    public static boolean isUsable(LocationConfiguration configuration, Location location) {
        if (location != null) {
            float givenAccuracy = location.getAccuracy();
            long givenTime = location.getTime();
            long minAcceptableTime = new Date().getTime() - configuration.defaultProviderConfiguration()
                  .acceptableTimePeriod();

            if (minAcceptableTime <= givenTime
                    && configuration.defaultProviderConfiguration().acceptableAccuracy() >= givenAccuracy) {
                return true;
            }
        }
        return false;
    }

    public static String getStringFromList(List<String> list) {
        String result = "[ ";
        int size = list.size();
        for (int i = 0; i < size; i++) {
            result += list.get(i);
            if (i == size - 1) {
                result += " ]";
            } else {
                result += ", ";
            }
        }
        return result;
    }
}
