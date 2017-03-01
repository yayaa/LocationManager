package com.yayandroid.locationmanager.providers.permissionprovider;

import android.content.pm.PackageManager;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;

import com.yayandroid.locationmanager.constants.RequestCode;
import com.yayandroid.locationmanager.helper.LogUtils;
import com.yayandroid.locationmanager.listener.DialogListener;
import com.yayandroid.locationmanager.providers.dialogprovider.DialogProvider;

public class DefaultPermissionProvider extends PermissionProvider {

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

        boolean shouldShowRationale = false;
        for (String permission : getRequiredPermissions()) {
            shouldShowRationale = shouldShowRationale || shouldShowRequestPermissionRationale(permission);
        }

        LogUtils.logI("Should show rationale dialog for required permissions: " + shouldShowRationale);
        if (shouldShowRationale && getActivity() != null && getDialogProvider() != null) {
            getDialogProvider().setDialogListener(new DialogListener() {
                @Override
                public void onPositiveButtonClick() {
                    executePermissionsRequest();
                }

                @Override
                public void onNegativeButtonClick() {
                    LogUtils.logI("User didn't even let us to ask for permission!");
                    if (getPermissionListener() != null) getPermissionListener().onPermissionsDenied();
                }
            });
            getDialogProvider().getDialog(getActivity()).show();
        } else {
            executePermissionsRequest();
        }

        return true;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == RequestCode.RUNTIME_PERMISSION) {

            // Check if any of required permissions are denied.
            boolean isDenied = false;
            for (int i = 0, size = permissions.length; i < size; i++) {
                if (grantResults[i] != PackageManager.PERMISSION_GRANTED) {
                    isDenied = true;
                }
            }

            if (isDenied) {
                LogUtils.logI("User denied some of required permissions, task will be aborted!");
                if (getPermissionListener() != null) getPermissionListener().onPermissionsDenied();
            } else {
                LogUtils.logI("We got all required permission!");
                if (getPermissionListener() != null) getPermissionListener().onPermissionsGranted();
            }
        }
    }

    private boolean shouldShowRequestPermissionRationale(String permission) {
        if (getFragment() != null) {
            return getFragment().shouldShowRequestPermissionRationale(permission);
        } else {
            return getActivity() != null && ActivityCompat.shouldShowRequestPermissionRationale(getActivity(), permission);
        }
    }

    private void executePermissionsRequest() {
        LogUtils.logI("Asking for Runtime Permissions...");
        if (getFragment() != null) {
            getFragment().requestPermissions(getRequiredPermissions(), RequestCode.RUNTIME_PERMISSION);
        } else if (getActivity() != null) {
            ActivityCompat.requestPermissions(getActivity(), getRequiredPermissions(), RequestCode.RUNTIME_PERMISSION);
        } else {
            LogUtils.logE("Something went wrong requesting for permissions.");
            if (getPermissionListener() != null) getPermissionListener().onPermissionsDenied();
        }
    }

}
