package com.yayandroid.locationmanager.providers.locationprovider;

import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.provider.Settings;
import android.support.annotation.NonNull;

import com.yayandroid.locationmanager.constants.FailType;
import com.yayandroid.locationmanager.constants.ProcessType;
import com.yayandroid.locationmanager.constants.RequestCode;
import com.yayandroid.locationmanager.helper.LocationUtils;
import com.yayandroid.locationmanager.helper.LogUtils;
import com.yayandroid.locationmanager.helper.continuoustask.ContinuousTask;
import com.yayandroid.locationmanager.helper.continuoustask.ContinuousTask.ContinuousTaskRunner;
import com.yayandroid.locationmanager.listener.DialogListener;
import com.yayandroid.locationmanager.providers.dialogprovider.DialogProvider;

@SuppressWarnings("ResourceType")
public class DefaultLocationProvider extends LocationProvider implements ContinuousTaskRunner, LocationListener {

    private static final String PROVIDER_SWITCH_TASK = "providerSwitchTask";
    private final ContinuousTask cancelTask = new ContinuousTask(PROVIDER_SWITCH_TASK, this);

    private String provider;
    private LocationManager locationManager;
    private UpdateRequest currentUpdateRequest;
    private Dialog gpsDialog;

    @Override
    public void onDestroy() {
        super.onDestroy();

        gpsDialog = null;

        if (currentUpdateRequest != null) {
            currentUpdateRequest.destroy();
            currentUpdateRequest = null;
        }

        if (locationManager != null) {
            locationManager.removeUpdates(this);
            locationManager = null;
        }
    }

    @Override
    public boolean isDialogShowing() {
        return gpsDialog != null && gpsDialog.isShowing();
    }

    @Override
    public void get() {
        setWaiting(true);

        if (getContext() != null) {
            locationManager = (LocationManager) getContext().getSystemService(Context.LOCATION_SERVICE);
        } else {
            onLocationFailed(FailType.VIEW_DETACHED);
            return;
        }

        // First check for GPS
        if (isGPSProviderEnabled()) {
            LogUtils.logI("GPS is already enabled, getting location...");
            askForLocation(LocationManager.GPS_PROVIDER);
        } else {
            // GPS is not enabled,
            if (getConfiguration().defaultProviderConfiguration().askForGPSEnable() && getActivity() != null) {
                LogUtils.logI("GPS is not enabled, asking user to enable it...");
                askForEnableGPS();
            } else {
                LogUtils.logI("GPS is not enabled, moving on with Network...");
                getLocationByNetwork();
            }
        }
    }

    @Override
    public void cancel() {
        if (currentUpdateRequest != null) currentUpdateRequest.release();
        cancelTask.stop();
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == RequestCode.GPS_ENABLE) {
            if (isGPSProviderEnabled()) {
                onGPSActivated();
            } else {
                LogUtils.logI("User didn't activate GPS, so continue with Network Provider");
                getLocationByNetwork();
            }
        }
    }

    @Override
    public void onPause() {
        super.onPause();

        if (currentUpdateRequest != null) {
            currentUpdateRequest.release();
        }

        cancelTask.pause();
    }

    @Override
    public void onResume() {
        super.onResume();

        if (currentUpdateRequest != null) {
            currentUpdateRequest.run();
        }

        if (isWaiting()) {
            cancelTask.resume();
        }

        if (isDialogShowing() && isGPSProviderEnabled()) {
            // User activated GPS by going settings manually
            gpsDialog.dismiss();
            onGPSActivated();
        }
    }

    private void askForEnableGPS() {
        DialogProvider gpsDialogProvider = getConfiguration().defaultProviderConfiguration().getGpsDialogProvider();
        gpsDialogProvider.setDialogListener(new DialogListener() {
            @Override
            public void onPositiveButtonClick() {
                boolean activityStarted = startActivityForResult(new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS),
                      RequestCode.GPS_ENABLE);
                if (!activityStarted) {
                    onLocationFailed(FailType.VIEW_NOT_REQUIRED_TYPE);
                }
            }

            @Override
            public void onNegativeButtonClick() {
                LogUtils.logI("User didn't want to enable GPS, so continue with Network Provider");
                getLocationByNetwork();
            }
        });
        gpsDialog = gpsDialogProvider.getDialog(getActivity());
        gpsDialog.show();
    }

    private void onGPSActivated() {
        LogUtils.logI("User activated GPS, listen for location");
        askForLocation(LocationManager.GPS_PROVIDER);
    }

    private void getLocationByNetwork() {
        if (isNetworkProviderEnabled() && isNetworkAvailable()) {
            LogUtils.logI("Network is enabled, getting location...");
            askForLocation(LocationManager.NETWORK_PROVIDER);
        } else {
            LogUtils.logI("Network is not enabled, calling fail...");
            onLocationFailed(FailType.NETWORK_NOT_AVAILABLE);
        }
    }

    private void askForLocation(String provider) {
        cancelTask.stop();
        this.provider = provider;

        boolean locationIsAlreadyAvailable = false;
        Location lastKnownLocation = locationManager.getLastKnownLocation(provider);

        if (LocationUtils.isUsable(lastKnownLocation, getConfiguration().defaultProviderConfiguration()
              .acceptableTimePeriod(), getConfiguration().defaultProviderConfiguration().acceptableAccuracy())) {
            LogUtils.logI("LastKnowLocation is usable.");
            onLocationReceived(lastKnownLocation);
            locationIsAlreadyAvailable = true;
        } else {
            LogUtils.logI("LastKnowLocation is not usable.");
        }

        if (getConfiguration().keepTracking() || !locationIsAlreadyAvailable) {
            LogUtils.logI("Ask for location update...");
            notifyProcessChange();
            // Ask for immediate location update
            requestUpdateLocation(0, 0, !locationIsAlreadyAvailable);
        } else {
            LogUtils.logI("We got location, no need to ask for location updates.");
        }
    }

    private void notifyProcessChange() {
        if (getListener() != null) {
            getListener().onProcessTypeChanged(provider.equals(LocationManager.GPS_PROVIDER)
                  ? ProcessType.GETTING_LOCATION_FROM_GPS_PROVIDER
                  : ProcessType.GETTING_LOCATION_FROM_NETWORK_PROVIDER);
        }
    }

    private void requestUpdateLocation(long timeInterval, long distanceInterval, boolean setCancelTask) {
        if (setCancelTask) {
            cancelTask.delayed(getWaitPeriod());
        }

        currentUpdateRequest = new UpdateRequest(provider, timeInterval, distanceInterval, this);
        currentUpdateRequest.run();
    }

    private long getWaitPeriod() {
        return provider.equals(LocationManager.GPS_PROVIDER)
              ? getConfiguration().defaultProviderConfiguration().gpsWaitPeriod()
              : getConfiguration().defaultProviderConfiguration().networkWaitPeriod();
    }

    private boolean isNetworkAvailable() {
        return LocationUtils.isNetworkAvailable(getContext());
    }

    private boolean isNetworkProviderEnabled() {
        return locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER);
    }

    private boolean isGPSProviderEnabled() {
        return locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
    }

    private void onLocationReceived(Location location) {
        if (getListener() != null) {
            getListener().onLocationChanged(location);
        }
        setWaiting(false);
    }

    private void onLocationFailed(@FailType int type) {
        if (getListener() != null) {
            getListener().onLocationFailed(type);
        }
        setWaiting(false);
    }

    @Override
    public void onLocationChanged(Location location) {
        onLocationReceived(location);

        // Remove cancelLocationTask because we have already find location,
        // no need to switch or call fail
        cancelTask.stop();

        if (getConfiguration().keepTracking()) {
            requestUpdateLocation(getConfiguration().defaultProviderConfiguration().requiredTimeInterval(),
                  getConfiguration().defaultProviderConfiguration().requiredDistanceInterval(), false);
        } else {
            locationManager.removeUpdates(this);
        }
    }

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {
        if (getListener() != null) {
            getListener().onStatusChanged(provider, status, extras);
        }
    }

    @Override
    public void onProviderEnabled(String provider) {
        if (getListener() != null) {
            getListener().onProviderEnabled(provider);
        }
    }

    @Override
    public void onProviderDisabled(String provider) {
        if (getListener() != null) {
            getListener().onProviderDisabled(provider);
        }
    }

    @Override
    public void runScheduledTask(@NonNull String taskId) {
        if (taskId.equals(PROVIDER_SWITCH_TASK)) {
            if (currentUpdateRequest != null) {
                currentUpdateRequest.release();
            }

            if (provider.equals(LocationManager.GPS_PROVIDER)) {
                LogUtils.logI("We waited enough for GPS, switching to Network provider...");
                getLocationByNetwork();
            } else {
                LogUtils.logI("Network Provider is not provide location in required period, calling fail...");
                onLocationFailed(FailType.TIMEOUT);
            }
        }
    }

    private class UpdateRequest {

        private final String provider;
        private final long minTime;
        private final float minDistance;
        private LocationListener listener;

        public UpdateRequest(String provider, long minTime, float minDistance, LocationListener listener) {
            this.provider = provider;
            this.minTime = minTime;
            this.minDistance = minDistance;
            this.listener = listener;
        }

        public void run() {
            locationManager.requestLocationUpdates(provider, minTime, minDistance, listener);
        }

        public void release() {
            locationManager.removeUpdates(listener);
        }

        public void destroy() {
            release();
            listener = null;
        }
    }
}