package com.yayandroid.locationmanager.providers.permissionprovider;

import android.support.annotation.Nullable;

import com.yayandroid.locationmanager.LocationManager;
import com.yayandroid.locationmanager.listener.PermissionListener;
import com.yayandroid.locationmanager.providers.dialogprovider.DialogProvider;
import com.yayandroid.locationmanager.view.ContextProcessor;

public abstract class PermissionProvider {

    protected ContextProcessor contextProcessor;
    protected final String[] requiredPermissions;
    protected PermissionListener permissionListener;
    protected DialogProvider rationalDialogProvider;

    /**
     * This class is responsible to get required permissions, and notify {@linkplain LocationManager}.
     *
     * @param requiredPermissions are required, setting this field empty will {@throws IllegalStateException}
     * @param rationaleDialogProvider will be used to display rationale dialog when it is necessary. If this field is set
     * to null, then rationale dialog will not be displayed to user at all.
     */
    public PermissionProvider(String[] requiredPermissions, @Nullable DialogProvider rationaleDialogProvider) {
        if (requiredPermissions == null || requiredPermissions.length == 0) {
            throw new IllegalStateException("You cannot create PermissionProvider without any permission required.");
        }

        this.requiredPermissions = requiredPermissions;
        this.rationalDialogProvider = rationaleDialogProvider;
    }

    /**
     * This method will be called by {@linkplain LocationManager} internally
     */
    public void setContextProcessor(ContextProcessor contextProcessor) {
        this.contextProcessor = contextProcessor;
    }

    /**
     * This method will be called by {@linkplain LocationManager} internally
     */
    public void setPermissionListener(PermissionListener permissionListener) {
        this.permissionListener = permissionListener;
    }

    /**
     * Return true if required permissions are granted, false otherwise
     */
    public abstract boolean hasPermission();

    /**
     * Return true if it is possible to ask permission, false otherwise
     */
    public abstract boolean requestPermissions();

    /**
     * This method needs to be called when
     */
    public abstract void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults);
}
