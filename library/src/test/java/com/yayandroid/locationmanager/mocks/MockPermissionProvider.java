package com.yayandroid.locationmanager.mocks;

import android.content.pm.PackageManager;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.yayandroid.locationmanager.providers.dialogprovider.DialogProvider;
import com.yayandroid.locationmanager.providers.permissionprovider.PermissionProvider;

public class MockPermissionProvider extends PermissionProvider {

    private boolean requestPermissions = false;
    private boolean isPermissionGranted = false;

    public MockPermissionProvider(String[] requiredPermissions, @Nullable DialogProvider rationaleDialogProvider) {
        super(requiredPermissions, rationaleDialogProvider);
    }

    public void shouldSuccessOnRequest(boolean success) {
        this.requestPermissions = success;
    }

    public void grantPermission(boolean granted) {
        this.isPermissionGranted = granted;
    }

    @Override
    protected int checkSelfPermission(String permission) {
        return isPermissionGranted ? PackageManager.PERMISSION_GRANTED : PackageManager.PERMISSION_DENIED;
    }

    @Override
    public boolean requestPermissions() {
        return requestPermissions;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @Nullable String[] permissions, @NonNull int[] grantResults) {

    }

}
