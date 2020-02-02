package com.yayandroid.locationmanager.providers.permissionprovider;

import androidx.annotation.NonNull;

import com.yayandroid.locationmanager.configuration.Defaults;

public class StubPermissionProvider extends PermissionProvider {

    public StubPermissionProvider() {
        super(Defaults.LOCATION_PERMISSIONS, null);
    }

    @Override
    public boolean requestPermissions() {
        return false;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, @NonNull int[] grantResults) {
    }
}
