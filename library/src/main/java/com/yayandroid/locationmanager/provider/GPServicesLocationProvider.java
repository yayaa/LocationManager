package com.yayandroid.locationmanager.provider;

import android.app.Activity;
import android.content.Intent;
import android.content.IntentSender;
import android.location.Location;
import android.os.Bundle;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.PendingResult;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.location.LocationSettingsResult;
import com.google.android.gms.location.LocationSettingsStatusCodes;
import com.yayandroid.locationmanager.constants.FailType;
import com.yayandroid.locationmanager.constants.LogType;
import com.yayandroid.locationmanager.constants.RequestCode;
import com.yayandroid.locationmanager.helper.LocationUtils;
import com.yayandroid.locationmanager.helper.LogUtils;

/**
 * Created by Yahya Bayramoglu on 09/02/16.
 */
public class GPServicesLocationProvider extends LocationProvider implements LocationListener,
        GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener,
        ResultCallback<LocationSettingsResult> {

    private GoogleApiClient googleApiClient;
    private boolean settingsDialogIsOn = false;

    @Override
    public void onResume() {
        if (!settingsDialogIsOn && googleApiClient != null &&
                (isWaiting() || configuration.keepTracking())) {
            googleApiClient.connect();
        }
    }

    @Override
    public void onPause() {
        if (!settingsDialogIsOn && googleApiClient != null && googleApiClient.isConnected()) {
            googleApiClient.disconnect();
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        clearGoogleApiClient();
    }

    @Override
    public boolean requiresActivityResult() {
        // If we need to ask for settingsApi then we'll need to get onActivityResult callback
        return configuration.gpServicesConfiguration().askForSettingsApi();
    }

    @Override
    public boolean isDialogShowing() {
        return settingsDialogIsOn;
    }

    @Override
    public void get() {
        setWaiting(true);

        if (contextProcessor.isContextExist()) {
            googleApiClient = new GoogleApiClient.Builder(contextProcessor.getContext())
                  .addApi(LocationServices.API)
                  .addConnectionCallbacks(this)
                  .addOnConnectionFailedListener(this)
                  .build();
            googleApiClient.connect();
        } else {
            failed(FailType.VIEW_DETACHED);
        }
    }

    @Override
    public void cancel() {
        LogUtils.logI("Canceling GPServiceLocationProvider...", LogType.GENERAL);
        if (googleApiClient != null && googleApiClient.isConnected()) {
            LocationServices.FusedLocationApi.removeLocationUpdates(googleApiClient, this);
            googleApiClient.disconnect();
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == RequestCode.SETTINGS_API) {
            settingsDialogIsOn = false;

            if (resultCode == Activity.RESULT_OK) {
                LogUtils.logI("We got settings changed, requesting location update...", LogType.GENERAL);
                requestLocationUpdate();
            } else {
                LogUtils.logI("User denied settingsApi dialog, GP_Settings failing...", LogType.IMPORTANT);
                settingsApiFail(FailType.GP_SERVICES_SETTINGS_DENIED);
            }
        }

    }

    @SuppressWarnings("ResourceType")
    @Override
    public void onConnected(Bundle bundle) {
        LogUtils.logI("GoogleApiClient is connected.", LogType.GENERAL);
        boolean locationIsAlreadyAvailable = false;

        if (LocationServices.FusedLocationApi.getLocationAvailability(googleApiClient).isLocationAvailable()) {
            Location lastKnownLocation = LocationServices.FusedLocationApi.getLastLocation(googleApiClient);

            if (LocationUtils.isUsable(configuration, lastKnownLocation)) {
                LogUtils.logI("LastKnowLocation is usable.", LogType.IMPORTANT);
                onLocationChanged(lastKnownLocation);
                locationIsAlreadyAvailable = true;
            } else {
                LogUtils.logI("LastKnowLocation is not usable.", LogType.GENERAL);
            }
        }

        if (configuration.keepTracking() || !locationIsAlreadyAvailable) {
            LogUtils.logI("Ask for location update...", LogType.IMPORTANT);
            if (configuration.gpServicesConfiguration().askForSettingsApi()) {
                LogUtils.logI("Asking for SettingsApi...", LogType.IMPORTANT);
                askForSettingsApi();
            } else {
                LogUtils.logI("SettingsApi is not enabled, requesting for location update...", LogType.GENERAL);
                requestLocationUpdate();
            }
        } else {
            LogUtils.logI("We got location, no need to ask for location updates.", LogType.GENERAL);
        }
    }

    @Override
    public void onConnectionSuspended(int i) {
        if (!configuration.gpServicesConfiguration().failOnConnectionSuspended() && googleApiClient != null) {
            LogUtils.logI("GoogleApiClient connection is suspended, try to connect again.", LogType.IMPORTANT);
            googleApiClient.connect();
        } else {
            LogUtils.logI("GoogleApiClient connection is suspended, calling fail...", LogType.GENERAL);
            failed(FailType.GP_SERVICES_CONNECTION_FAIL);
        }
    }

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {
        LogUtils.logI("GoogleApiClient connection is failed.", LogType.GENERAL);
        failed(FailType.GP_SERVICES_CONNECTION_FAIL);
    }

    @Override
    public void onLocationChanged(Location location) {
        if (listener != null) {
            listener.onLocationChanged(location);
        }

        // Set waiting as false because we got at least one, even though we keep tracking user's location
        setWaiting(false);

        if (!configuration.keepTracking()) {
            LogUtils.logI("We got location and no need to keep tracking, so location update is removed.", LogType.GENERAL);
            LocationServices.FusedLocationApi.removeLocationUpdates(googleApiClient, this);
        }
    }

    @Override
    public void onResult(LocationSettingsResult result) {
        final Status status = result.getStatus();

        switch (status.getStatusCode()) {
            case LocationSettingsStatusCodes.SUCCESS:
                // All location settings are satisfied. The client can initialize location
                // requests here.
                LogUtils.logI("We got GPS, Wifi and/or Cell network providers enabled enough " +
                        "to receive location as we needed. Requesting location update...", LogType.GENERAL);
                requestLocationUpdate();
                break;
            case LocationSettingsStatusCodes.RESOLUTION_REQUIRED:
                // Location settings are not satisfied. But could be fixed by showing the user
                // a dialog.
                try {
                    // Show the dialog by calling startResolutionForResult(),
                    // and check the result in onActivityResult().
                    LogUtils.logI("We need settingsApi to display dialog to switch required settings on, displaying the dialog...", LogType.GENERAL);
                    settingsDialogIsOn = true;
                    if (contextProcessor.isActivityExist()) {
                        status.startResolutionForResult(contextProcessor.getActivity(), RequestCode.SETTINGS_API);
                    } else {
                        settingsApiFail(FailType.VIEW_DETACHED);
                    }
                } catch (IntentSender.SendIntentException e) {
                    LogUtils.logE("Error on displaying SettingsApi dialog, GP_SettingsApi failing...", LogType.IMPORTANT);
                    settingsApiFail(FailType.GP_SERVICES_SETTINGS_DIALOG);
                }
                break;
            case LocationSettingsStatusCodes.SETTINGS_CHANGE_UNAVAILABLE:
                // Location settings are not satisfied. However, we have no way to fix the
                // settings so we won't show the dialog.
                LogUtils.logE("Settings change is not available, GP_SettingsApi failing...", LogType.IMPORTANT);
                settingsApiFail(FailType.GP_SERVICES_SETTINGS_DIALOG);
                break;
        }
    }

    private void askForSettingsApi() {
        LocationSettingsRequest settingsRequest = new LocationSettingsRequest.Builder()
                .addLocationRequest(configuration.gpServicesConfiguration().locationRequest())
                .build();

        PendingResult<LocationSettingsResult> settingsResult = LocationServices.SettingsApi
              .checkLocationSettings(googleApiClient, settingsRequest);
        settingsResult.setResultCallback(this);
    }

    @SuppressWarnings("ResourceType")
    private void requestLocationUpdate() {
        LocationServices.FusedLocationApi.requestLocationUpdates(googleApiClient,
              configuration.gpServicesConfiguration().locationRequest(), this);
    }

    private void settingsApiFail(int failType) {
        if (configuration.gpServicesConfiguration().failOnSettingsApiSuspended()) {
            failed(failType);
        } else {
            LogUtils.logE("Even though settingsApi failed, configuration requires moving on, " +
                    "so requesting location update...", LogType.GENERAL);
            if (googleApiClient.isConnected()) {
                requestLocationUpdate();
            } else {
                LogUtils.logE("GoogleApiClient is not connected. Aborting...", LogType.IMPORTANT);
                failed(failType);
            }
        }
    }

    private void failed(int type) {
        if (listener != null) {
            listener.onLocationFailed(type);
        }
        setWaiting(false);
    }

    private void clearGoogleApiClient() {
        // Clear in any possible way to prevent memory leaks
        if (googleApiClient != null) {
            googleApiClient.unregisterConnectionCallbacks(this);
            googleApiClient.unregisterConnectionFailedListener(this);

            if (googleApiClient.isConnected()) {
                LocationServices.FusedLocationApi.removeLocationUpdates(googleApiClient, this);
            }

            googleApiClient.disconnect();
            googleApiClient = null;
        }
    }
}