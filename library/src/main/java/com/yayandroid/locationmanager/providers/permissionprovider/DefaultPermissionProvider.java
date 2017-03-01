package com.yayandroid.locationmanager.providers.permissionprovider;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.yayandroid.locationmanager.helper.LocationUtils;
import com.yayandroid.locationmanager.helper.LogUtils;
import com.yayandroid.locationmanager.helper.PermissionManager;
import com.yayandroid.locationmanager.providers.dialogprovider.DialogProvider;

import java.util.List;

public class DefaultPermissionProvider extends PermissionProvider implements PermissionManager.PermissionListener {

    public DefaultPermissionProvider(String[] requiredPermissions, @Nullable DialogProvider dialogProvider) {
        super(requiredPermissions, dialogProvider);
    }

    @Override
    public boolean requestPermissions() {
        if (getActivity() == null) {
            LogUtils.logI("Cannot ask for permissions, "
                  + "because DefaultPermissionProvider doesn't contain an Activity instance.");
            return false;
        }

        LogUtils.logI("Asking for Runtime Permissions...");

        PermissionManager.requestPermissions(getFragment() != null ? getFragment() : getActivity(),
              this, getDialogProvider(), getRequiredPermissions());
        return true;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, @NonNull int[] grantResults) {
        PermissionManager.onRequestPermissionsResult(this, requestCode, permissions, grantResults);
    }

    @Override
    public void onPermissionsGranted(List<String> perms) {
        if (perms.size() == getRequiredPermissions().length) {
            LogUtils.logI("We got all required permission!");
            if (getPermissionListener() != null) getPermissionListener().onPermissionsGranted();
        } else {
            LogUtils.logI("User denied some of required permissions! "
                  + "Even though we have following permissions now, "
                  + "task will still be aborted.\n" + LocationUtils.getStringFromList(perms));
            if (getPermissionListener() != null) getPermissionListener().onPermissionsDenied();
        }
    }

    @Override
    public void onPermissionsDenied(List<String> perms) {
        LogUtils.logI("User denied required permissions!\n" + LocationUtils.getStringFromList(perms));
        if (getPermissionListener() != null) getPermissionListener().onPermissionsDenied();
    }

    @Override
    public void onPermissionRequestRejected() {
        LogUtils.logI("User didn't even let us to ask for permission!");
        if (getPermissionListener() != null) getPermissionListener().onPermissionsDenied();
    }
}
