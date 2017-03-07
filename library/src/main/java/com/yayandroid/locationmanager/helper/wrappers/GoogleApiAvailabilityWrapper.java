package com.yayandroid.locationmanager.helper.wrappers;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface.OnCancelListener;
import android.support.annotation.Nullable;

import com.google.android.gms.common.GoogleApiAvailability;

public class GoogleApiAvailabilityWrapper {

    public int isAvailable(Context context) {
        if (context == null) return -1;
        return GoogleApiAvailability.getInstance().isGooglePlayServicesAvailable(context);
    }

    public boolean isUserResolvableError(int gpServicesAvailability) {
        return GoogleApiAvailability.getInstance().isUserResolvableError(gpServicesAvailability);
    }

    @Nullable
    public Dialog getErrorDialog(Activity activity, int gpServicesAvailability,
          int requestCode, OnCancelListener onCancelListener) {
        if (activity == null) return null;
        return GoogleApiAvailability.getInstance()
              .getErrorDialog(activity, gpServicesAvailability, requestCode, onCancelListener);
    }

}
