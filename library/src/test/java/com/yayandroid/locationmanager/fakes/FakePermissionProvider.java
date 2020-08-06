package com.yayandroid.locationmanager.fakes;

import android.content.pm.PackageManager;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.yayandroid.locationmanager.providers.dialogprovider.DialogProvider;
import com.yayandroid.locationmanager.providers.permissionprovider.PermissionProvider;

public class FakePermissionProvider extends PermissionProvider {

    private boolean requestPermissions = false;
    private boolean isPermissionGranted = false;

    public FakePermissionProvider(String[] requiredPermissions, @Nullable DialogProvider rationaleDialogProvider) {
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
