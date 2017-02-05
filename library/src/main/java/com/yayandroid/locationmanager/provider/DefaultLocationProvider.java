package com.yayandroid.locationmanager.provider;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.provider.Settings;
import android.support.v7.app.AlertDialog;

import com.yayandroid.locationmanager.constants.FailType;
import com.yayandroid.locationmanager.constants.LogType;
import com.yayandroid.locationmanager.constants.RequestCode;
import com.yayandroid.locationmanager.helper.ContinuousTask;
import com.yayandroid.locationmanager.helper.LocationUtils;
import com.yayandroid.locationmanager.helper.LogUtils;

/**
 * Created by Yahya Bayramoglu on 09/02/16.
 */
@SuppressWarnings("ResourceType")
public class DefaultLocationProvider extends LocationProvider {

    private String provider;
    private LocationManager locationManager;
    private UpdateRequest currentUpdateRequest;
    private AlertDialog gpsDialog;

    @Override
    public void onCreate() {
        super.onCreate();

        if (contextProcessor.isContextExist()) {
            this.locationManager = (LocationManager) contextProcessor.getContext()
                  .getSystemService(Context.LOCATION_SERVICE);
        } else {
            onLocationFailed(FailType.VIEW_DETACHED);
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        gpsDialog = null;

        if (currentUpdateRequest != null) {
            currentUpdateRequest.destroy();
            currentUpdateRequest = null;
        }

        if (locationManager != null) {
            locationManager.removeUpdates(locationChangeListener);
            locationManager = null;
        }
    }

    @Override
    public boolean requiresActivityResult() {
        // If we need to ask for enabling GPS then we'll need to get onActivityResult callback
        return configuration.defaultProviderConfiguration().askForGPSEnable();
    }

    @Override
    public boolean isDialogShowing() {
        return (gpsDialog != null && gpsDialog.isShowing());
    }

    @Override
    public void get() {
        setWaiting(true);

        // First check for GPS
        if (isGPSProviderEnabled()) {
            LogUtils.logI("GPS is already enabled, getting location...", LogType.GENERAL);
            askForLocation(LocationManager.GPS_PROVIDER);
        } else {
            // GPS is not enabled,
            if (configuration.defaultProviderConfiguration().askForGPSEnable() && contextProcessor.isActivityExist()) {
                LogUtils.logI("GPS is not enabled, asking user to enable it...", LogType.GENERAL);
                askForEnableGPS();
            } else {
                LogUtils.logI("GPS is not enabled, moving on with Network...", LogType.GENERAL);
                getLocationByNetwork();
            }
        }
    }

    @Override
    public void cancel() {
        if (currentUpdateRequest != null) {
            currentUpdateRequest.release();
        }

        cancelTask.stop();
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == RequestCode.GPS_ENABLE) {
            if (isGPSProviderEnabled()) {
                LogUtils.logI("User activated GPS, listen for location", LogType.GENERAL);
                askForLocation(LocationManager.GPS_PROVIDER);
            } else {
                LogUtils.logI("User didn't activate GPS, so continue with Network Provider", LogType.IMPORTANT);
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
            LogUtils.logI("User activated GPS, listen for location", LogType.GENERAL);
            askForLocation(LocationManager.GPS_PROVIDER);
        }
    }

    private void askForEnableGPS() {
        gpsDialog = new AlertDialog.Builder(contextProcessor.getActivity())
              .setMessage(configuration.defaultProviderConfiguration().gpsMessage())
              .setCancelable(false)
              .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                  @Override
                  public void onClick(DialogInterface dialog, int which) {
                      if (contextProcessor.isActivityExist()) {
                          contextProcessor.getActivity()
                                .startActivityForResult(new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS),
                                      RequestCode.GPS_ENABLE);
                      }
                  }
              })
              .setNegativeButton(android.R.string.cancel, new DialogInterface.OnClickListener() {
                  @Override
                  public void onClick(DialogInterface dialog, int which) {
                      LogUtils.logI("User didn't want to enable GPS, so continue with Network Provider",
                            LogType.IMPORTANT);
                      getLocationByNetwork();
                  }
              })
              .create();

        gpsDialog.show();
    }

    private void getLocationByNetwork() {
        if (isNetworkProviderEnabled() && isNetworkAvailable()) {
            LogUtils.logI("Network is enabled, getting location...", LogType.GENERAL);
            askForLocation(LocationManager.NETWORK_PROVIDER);
        } else {
            LogUtils.logI("Network is not enabled, calling fail...", LogType.GENERAL);
            onLocationFailed(FailType.NETWORK_NOT_AVAILABLE);
        }
    }

    private void askForLocation(String provider) {
        cancelTask.stop();
        this.provider = provider;

        boolean locationIsAlreadyAvailable = false;
        Location lastKnownLocation = locationManager.getLastKnownLocation(provider);

        if (LocationUtils.isUsable(configuration, lastKnownLocation)) {
            LogUtils.logI("LastKnowLocation is usable.", LogType.IMPORTANT);
            onLocationReceived(lastKnownLocation);
            locationIsAlreadyAvailable = true;
        } else {
            LogUtils.logI("LastKnowLocation is not usable.", LogType.GENERAL);
        }

        if (configuration.keepTracking() || !locationIsAlreadyAvailable) {
            LogUtils.logI("Ask for location update...", LogType.IMPORTANT);
            requestUpdateLocation(0, !locationIsAlreadyAvailable); // Ask for immediately
        } else {
            LogUtils.logI("We got location, no need to ask for location updates.", LogType.GENERAL);
        }
    }

    private void requestUpdateLocation(long timeInterval, boolean setCancelTask) {
        if (setCancelTask) {
            cancelTask.delayed(getWaitPeriod());
        }

        currentUpdateRequest = new UpdateRequest(provider, timeInterval, 0, locationChangeListener);
        currentUpdateRequest.run();
    }

    private long getWaitPeriod() {
        return provider.equals(LocationManager.GPS_PROVIDER)
              ? configuration.defaultProviderConfiguration().gpsWaitPeriod()
              : configuration.defaultProviderConfiguration().networkWaitPeriod();
    }

    private boolean isNetworkAvailable() {
        return LocationUtils.isNetworkAvailable(contextProcessor.getContext());
    }

    private boolean isNetworkProviderEnabled() {
        return locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER);
    }

    private boolean isGPSProviderEnabled() {
        return locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
    }

    private void onLocationReceived(Location location) {
        if (listener != null) {
            listener.onLocationChanged(location);
        }
        setWaiting(false);
    }

    private void onLocationFailed(int type) {
        if (listener != null) {
            listener.onLocationFailed(type);
        }
        setWaiting(false);
    }

    private final LocationListener locationChangeListener = new LocationListener() {

        @Override
        public void onLocationChanged(Location location) {
            onLocationReceived(location);

            // Remove cancelLocationTask because we have already find location,
            // no need to switch or call fail
            cancelTask.stop();

            if (configuration.keepTracking()) {
                requestUpdateLocation(configuration.requiredTimeInterval(), false);
            } else {
                locationManager.removeUpdates(locationChangeListener);
            }
        }

        @Override
        public void onStatusChanged(String provider, int status, Bundle extras) {
            if (listener != null) {
                listener.onStatusChanged(provider, status, extras);
            }
        }

        @Override
        public void onProviderEnabled(String provider) {
            if (listener != null) {
                listener.onProviderEnabled(provider);
            }
        }

        @Override
        public void onProviderDisabled(String provider) {
            if (listener != null) {
                listener.onProviderDisabled(provider);
            }
        }
    };

    private class UpdateRequest {

        private String provider;
        private long minTime;
        private float minDistance;
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
            this.listener = null;
        }
    }

    private final ContinuousTask cancelTask = new ContinuousTask() {

        @Override
        public void run() {
            if (currentUpdateRequest != null) {
                currentUpdateRequest.release();
            }

            if (provider.equals(LocationManager.GPS_PROVIDER)) {
                LogUtils.logI("We waited enough for GPS, switching to Network provider...", LogType.IMPORTANT);
                getLocationByNetwork();
            } else {
                LogUtils.logI("Network Provider is not provide location in required period, calling fail...",
                      LogType.GENERAL);
                onLocationFailed(FailType.TIMEOUT);
            }
        }
    };
}