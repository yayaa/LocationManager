package com.yayandroid.locationmanager.providers.permissionprovider;

import android.app.Activity;

import androidx.core.app.ActivityCompat;
import androidx.fragment.app.Fragment;

class PermissionCompatSource {

    boolean shouldShowRequestPermissionRationale(Fragment fragment, String permission) {
        return fragment.shouldShowRequestPermissionRationale(permission);
    }

    void requestPermissions(Fragment fragment, String[] requiredPermissions, int requestCode) {
        fragment.requestPermissions(requiredPermissions, requestCode);
    }

    boolean shouldShowRequestPermissionRationale(Activity activity, String permission) {
        return ActivityCompat.shouldShowRequestPermissionRationale(activity, permission);
    }

    void requestPermissions(Activity activity, String[] requiredPermissions, int requestCode) {
        ActivityCompat.requestPermissions(activity, requiredPermissions, requestCode);
    }

}
