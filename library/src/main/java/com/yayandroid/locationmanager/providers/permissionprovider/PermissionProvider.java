package com.yayandroid.locationmanager.providers.permissionprovider;

import android.support.annotation.Nullable;

import com.yayandroid.locationmanager.listener.PermissionListener;
import com.yayandroid.locationmanager.providers.dialogprovider.RationaleDialogProvider;
import com.yayandroid.locationmanager.view.ContextProcessor;

public abstract class PermissionProvider {

    protected final ContextProcessor contextProcessor;
    protected final PermissionListener permissionListener;
    protected final String[] requiredPermissions;
    @Nullable protected final String rationalMessage;

    public PermissionProvider(ContextProcessor contextProcessor, PermissionListener permissionListener,
          String[] requiredPermissions, @Nullable String rationalMessage) {

        if (permissionListener == null)
            throw new IllegalStateException("PermissionListener cannot be null");

        if (requiredPermissions == null || requiredPermissions.length == 0)
            throw new IllegalStateException("You cannot create PermissionProvider without any permission required.");

        this.contextProcessor = contextProcessor;
        this.requiredPermissions = requiredPermissions;
        this.permissionListener = permissionListener;
        this.rationalMessage = rationalMessage;
    }

    /**
     * Return true if required permissions are granted, false otherwise
     */
    public abstract boolean hasPermission();

    /**
     * Return true if it is possible to ask permission, false otherwise
     */
    public abstract boolean requestPermissions(@Nullable RationaleDialogProvider rationaleDialogProvider);

    public abstract void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults);

}
