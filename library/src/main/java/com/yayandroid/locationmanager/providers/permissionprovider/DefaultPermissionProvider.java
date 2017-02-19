package com.yayandroid.locationmanager.providers.permissionprovider;

import android.support.annotation.Nullable;

import com.yayandroid.locationmanager.constants.LogType;
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
        if (contextProcessor.getActivity() == null) {
            LogUtils.logI("Cannot ask for location, because contextProcessor doesn't contain an Activity instance.",
                  LogType.GENERAL);
            return false;
        }

        LogUtils.logI("Asking for Runtime Permissions...", LogType.GENERAL);

        PermissionManager.requestPermissions(
              contextProcessor.getFragment() != null ? contextProcessor.getFragment() : contextProcessor.getActivity(),
              this, rationalDialogProvider, requiredPermissions);
        return true;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        PermissionManager.onRequestPermissionsResult(this, requestCode, permissions, grantResults);
    }

    @Override
    public void onPermissionsGranted(List<String> perms) {
        if (perms.size() == requiredPermissions.length) {
            LogUtils.logI("We got all required permission!", LogType.GENERAL);
            permissionListener.onPermissionsGranted();
        } else {
            LogUtils.logI("User denied some of required permissions! "
                  + "Even though we have following permissions now, "
                  + "task will still be aborted.\n" + LocationUtils.getStringFromList(perms), LogType.GENERAL);
            permissionListener.onPermissionsDenied();
        }
    }

    @Override
    public void onPermissionsDenied(List<String> perms) {
        LogUtils.logI("User denied required permissions!\n" + LocationUtils.getStringFromList(perms), LogType.IMPORTANT);
        permissionListener.onPermissionsDenied();
    }

    @Override
    public void onPermissionRequestRejected() {
        LogUtils.logI("User didn't even let us to ask for permission!", LogType.IMPORTANT);
        permissionListener.onPermissionsDenied();
    }
}
