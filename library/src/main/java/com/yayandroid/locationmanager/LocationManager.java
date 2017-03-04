package com.yayandroid.locationmanager;

import android.content.Context;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;

import com.yayandroid.locationmanager.configuration.LocationConfiguration;
import com.yayandroid.locationmanager.constants.FailType;
import com.yayandroid.locationmanager.helper.LogUtils;
import com.yayandroid.locationmanager.listener.LocationListener;
import com.yayandroid.locationmanager.listener.PermissionListener;
import com.yayandroid.locationmanager.providers.locationprovider.DefaultLocationProvider;
import com.yayandroid.locationmanager.providers.locationprovider.DispatcherLocationProvider;
import com.yayandroid.locationmanager.providers.locationprovider.LocationProvider;
import com.yayandroid.locationmanager.providers.permissionprovider.PermissionProvider;
import com.yayandroid.locationmanager.view.ContextProcessor;

public class LocationManager implements PermissionListener {

    private ContextProcessor contextProcessor;
    private LocationListener listener;
    private LocationConfiguration configuration;
    private LocationProvider activeProvider;
    private PermissionProvider permissionProvider;

    /**
     * Library tries to log as much as possible in order to make it transparent to see what is actually going on
     * under the hood. You can enable it for debug purposes, but do not forget to disable on production.
     *
     * Log is disabled as default.
     */
    public static void enableLog(boolean enable) {
        LogUtils.enable(enable);
    }

    /**
     * To create an instance of this manager you MUST specify a LocationConfiguration
     */
    public LocationManager(@NonNull LocationConfiguration configuration) {
        this.configuration = configuration;
    }

    /**
     * This specifies on which context this manager will run,
     * this also needs to be set before you attempt to get location
     */
    public LocationManager on(@NonNull Context context) {
        if (contextProcessor != null) throw new IllegalStateException("on method can be called only once.");
        this.contextProcessor = new ContextProcessor(context);
        return this;
    }

    /**
     * This specifies on which context this manager will run,
     * this also needs to be set before you attempt to get location
     */
    public LocationManager on(@NonNull Fragment fragment) {
        if (contextProcessor != null) throw new IllegalStateException("on method can be called only once.");
        this.contextProcessor = new ContextProcessor(fragment);
        return this;
    }

    /**
     * Specify a LocationListener to receive location when it is available,
     * or get knowledge of any other steps in process
     */
    public LocationManager notify(LocationListener listener) {
        this.listener = listener;
        return this;
    }

    /**
     * Instead of using {@linkplain DefaultLocationProvider} you can create your own,
     * and set it to manager so it will use given one.
     */
    public LocationManager setLocationProvider(LocationProvider provider) {
        if (provider != null) {
            provider.configure(contextProcessor, configuration);
        }

        this.activeProvider = provider;
        return this;
    }

    /**
     * Returns configuration object which is defined to this manager
     */
    public LocationConfiguration getConfiguration() {
        return configuration;
    }

    /**
     * Google suggests to stop location updates when the activity is no longer in focus
     * http://developer.android.com/training/location/receive-location-updates.html#stop-updates
     */
    public void onPause() {
        if (activeProvider != null) {
            activeProvider.onPause();
        }
    }

    /**
     * Restart location updates to keep continue getting locations when activity is back
     */
    public void onResume() {
        if (activeProvider != null) {
            activeProvider.onResume();
        }
    }

    /**
     * Release whatever you need to when activity is destroyed
     */
    public void onDestroy() {
        if (activeProvider != null) {
            activeProvider.onDestroy();
        }

        listener = null;
        contextProcessor = null;
        activeProvider = null;
        configuration = null;
    }

    /**
     * This is required to check when user handles with Google Play Services error, or enables GPS...
     */
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (activeProvider != null) {
            activeProvider.onActivityResult(requestCode, resultCode, data);
        }
    }

    /**
     * Provide requestPermissionResult to manager so the it can handle RuntimePermission
     */
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        getPermissionProvider().onRequestPermissionsResult(requestCode, permissions, grantResults);
    }

    /**
     * To determine whether LocationManager is currently waiting for location or it did already receive one!
     */
    public boolean isWaitingForLocation() {
        return activeProvider != null && activeProvider.isWaiting();
    }

    /**
     * To determine whether the manager is currently displaying any dialog or not
     */
    public boolean isAnyDialogShowing() {
        return activeProvider != null && activeProvider.isDialogShowing();
    }

    /**
     * Abort the mission and cancel all location update requests
     */
    public void cancel() {
        if (activeProvider != null) {
            activeProvider.cancel();
        }
    }

    /**
     * The only method you need to call to trigger getting location process
     */
    public void get() {
        askForPermission();
    }

    private void askForPermission() {
        if (getPermissionProvider().hasPermission()) {
            permissionGranted(true);
        } else {
            if (getPermissionProvider().requestPermissions()) {
                LogUtils.logI("Waiting until we receive any callback from PermissionProvider...");
            } else {
                LogUtils.logI("Couldn't get permission, Abort!");
                failed(FailType.PERMISSION_DENIED);
            }
        }
    }

    private void permissionGranted(boolean alreadyHadPermission) {
        LogUtils.logI("We got permission!");

        if (listener != null) {
            listener.onPermissionGranted(alreadyHadPermission);
        }

        getLocation();
    }

    private void getLocation() {
        getActiveProvider().notifyTo(listener);
        getActiveProvider().get();
    }

    private LocationProvider getActiveProvider() {
        if (activeProvider == null) {
            setLocationProvider(new DispatcherLocationProvider());
        }
        return activeProvider;
    }

    private PermissionProvider getPermissionProvider() {
        if (permissionProvider == null) {
            permissionProvider = getConfiguration().permissionConfiguration().permissionProvider();
            permissionProvider.setContextProcessor(contextProcessor);
            permissionProvider.setPermissionListener(this);
        }
        return permissionProvider;
    }

    private void failed(@FailType.Reason int type) {
        if (listener != null) {
            listener.onLocationFailed(type);
        }
    }

    @Override
    public void onPermissionsGranted() {
        permissionGranted(false);
    }

    @Override
    public void onPermissionsDenied() {
        failed(FailType.PERMISSION_DENIED);
    }
}