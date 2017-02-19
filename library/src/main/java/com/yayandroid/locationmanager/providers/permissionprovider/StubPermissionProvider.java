package com.yayandroid.locationmanager.providers.permissionprovider;

import com.yayandroid.locationmanager.configuration.Defaults;
import com.yayandroid.locationmanager.constants.LogType;
import com.yayandroid.locationmanager.helper.LogUtils;
import com.yayandroid.locationmanager.helper.PermissionManager;

public class StubPermissionProvider extends PermissionProvider {

    public StubPermissionProvider() {
        super(Defaults.LOCATION_PERMISSIONS, null);
    }

    @Override
    public boolean hasPermission() {
        if (contextProcessor.getContext() == null) {
            LogUtils.logE("Couldn't check whether permissions are granted or not "
                  + "because of contextProcessor doesn't contain any context.", LogType.IMPORTANT);
            return false;
        }

        return PermissionManager.hasPermissions(contextProcessor.getContext(), requiredPermissions);
    }

    @Override
    public boolean requestPermissions() {
        return false;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
    }
}
