package com.yayandroid.locationmanager.helper;

import android.content.Context;
import android.location.Location;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

import com.yayandroid.locationmanager.configuration.LocationConfiguration;

import java.util.Date;
import java.util.List;

/**
 * Created by Yahya Bayramoglu on 10/02/16.
 */
public class LocationUtils {

    public static boolean isNetworkAvailable(Context context) {
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
