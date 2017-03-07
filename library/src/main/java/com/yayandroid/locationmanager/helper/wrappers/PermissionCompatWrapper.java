package com.yayandroid.locationmanager.helper.wrappers;

import android.app.Activity;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;

public class PermissionCompatWrapper {

    public boolean shouldShowRequestPermissionRationale(Fragment fragment, String permission) {
        return fragment.shouldShowRequestPermissionRationale(permission);
    }

    public void requestPermissions(Fragment fragment, String[] requiredPermissions, int requestCode) {
        fragment.requestPermissions(requiredPermissions, requestCode);
    }

    public boolean shouldShowRequestPermissionRationale(Activity activity, String permission) {
        return ActivityCompat.shouldShowRequestPermissionRationale(activity, permission);
    }

    public void requestPermissions(Activity activity, String[] requiredPermissions, int requestCode) {
        ActivityCompat.requestPermissions(activity, requiredPermissions, requestCode);
    }

}
