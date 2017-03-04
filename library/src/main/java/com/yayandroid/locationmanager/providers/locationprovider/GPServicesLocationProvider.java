package com.yayandroid.locationmanager.providers.locationprovider;

import android.app.Activity;
import android.content.Intent;
import android.content.IntentSender;
import android.location.Location;
import android.os.Bundle;
import android.support.annotation.NonNull;

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
import com.yayandroid.locationmanager.constants.RequestCode;
import com.yayandroid.locationmanager.helper.LocationUtils;
import com.yayandroid.locationmanager.helper.LogUtils;

public class GPServicesLocationProvider extends LocationProvider implements LocationListener,
        GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener,
        ResultCallback<LocationSettingsResult> {

    private GoogleApiClient googleApiClient;
    private boolean settingsDialogIsOn = false;

    @Override
    public void onResume() {
        if (!settingsDialogIsOn && googleApiClient != null &&
                (isWaiting() || getConfiguration().keepTracking())) {
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
    public boolean isDialogShowing() {
        return settingsDialogIsOn;
    }

    @Override
    public void get() {
        setWaiting(true);

        if (getContext() != null) {
            googleApiClient = new GoogleApiClient.Builder(getContext())
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
        LogUtils.logI("Canceling GPServiceLocationProvider...");
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
                LogUtils.logI("We got settings changed, requesting location update...");
                requestLocationUpdate();
            } else {
                LogUtils.logI("User denied settingsApi dialog, GP_Settings failing...");
                settingsApiFail(FailType.GP_SERVICES_SETTINGS_DENIED);
            }
        }

    }

    @SuppressWarnings("ResourceType")
    @Override
    public void onConnected(Bundle bundle) {
        LogUtils.logI("GoogleApiClient is connected.");
        boolean locationIsAlreadyAvailable = false;

        if (LocationServices.FusedLocationApi.getLocationAvailability(googleApiClient).isLocationAvailable()) {
            Location lastKnownLocation = LocationServices.FusedLocationApi.getLastLocation(googleApiClient);

            if (LocationUtils.isUsable(getConfiguration(), lastKnownLocation)) {
                LogUtils.logI("LastKnowLocation is usable.");
                onLocationChanged(lastKnownLocation);
                locationIsAlreadyAvailable = true;
            } else {
                LogUtils.logI("LastKnowLocation is not usable.");
            }
        }

        if (getConfiguration().keepTracking() || !locationIsAlreadyAvailable) {
            LogUtils.logI("Ask for location update...");
            if (getConfiguration().gpServicesConfiguration().askForSettingsApi()) {
                LogUtils.logI("Asking for SettingsApi...");
                askForSettingsApi();
            } else {
                LogUtils.logI("SettingsApi is not enabled, requesting for location update...");
                requestLocationUpdate();
            }
        } else {
            LogUtils.logI("We got location, no need to ask for location updates.");
        }
    }

    @Override
    public void onConnectionSuspended(int i) {
        if (!getConfiguration().gpServicesConfiguration().failOnConnectionSuspended() && googleApiClient != null) {
            LogUtils.logI("GoogleApiClient connection is suspended, try to connect again.");
            googleApiClient.connect();
        } else {
            LogUtils.logI("GoogleApiClient connection is suspended, calling fail...");
            failed(FailType.GP_SERVICES_CONNECTION_FAIL);
        }
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
        LogUtils.logI("GoogleApiClient connection is failed.");
        failed(FailType.GP_SERVICES_CONNECTION_FAIL);
    }

    @Override
    public void onLocationChanged(Location location) {
        if (getListener() != null) {
            getListener().onLocationChanged(location);
        }

        // Set waiting as false because we got at least one, even though we keep tracking user's location
        setWaiting(false);

        if (!getConfiguration().keepTracking()) {
            LogUtils.logI("We got location and no need to keep tracking, so location update is removed.");
            LocationServices.FusedLocationApi.removeLocationUpdates(googleApiClient, this);
        }
    }

    @Override
    public void onResult(@NonNull LocationSettingsResult result) {
        final Status status = result.getStatus();

        switch (status.getStatusCode()) {
            case LocationSettingsStatusCodes.SUCCESS:
                // All location settings are satisfied. The client can initialize location
                // requests here.
                LogUtils.logI("We got GPS, Wifi and/or Cell network providers enabled enough "
                      + "to receive location as we needed. Requesting location update...");
                requestLocationUpdate();
                break;
            case LocationSettingsStatusCodes.RESOLUTION_REQUIRED:
                // Location settings are not satisfied. But could be fixed by showing the user
                // a dialog.
                try {
                    // Show the dialog by calling startResolutionForResult(),
                    // and check the result in onActivityResult().
                    LogUtils.logI("We need settingsApi dialog to switch required settings on, displaying the dialog...");
                    settingsDialogIsOn = true;

                    if (getActivity() != null) {
                        status.startResolutionForResult(getActivity(), RequestCode.SETTINGS_API);
                    } else {
                        settingsApiFail(FailType.VIEW_NOT_REQUIRED_TYPE);
                    }
                } catch (IntentSender.SendIntentException e) {
                    LogUtils.logE("Error on displaying SettingsApi dialog, SettingsApi failing...");
                    settingsApiFail(FailType.GP_SERVICES_SETTINGS_DIALOG);
                }
                break;
            case LocationSettingsStatusCodes.SETTINGS_CHANGE_UNAVAILABLE:
                // Location settings are not satisfied.
                // However, we have no way to fix the settings so we won't show the dialog.
                LogUtils.logE("Settings change is not available, SettingsApi failing...");
                settingsApiFail(FailType.GP_SERVICES_SETTINGS_DIALOG);
                break;
        }
    }

    private void askForSettingsApi() {
        LocationSettingsRequest settingsRequest = new LocationSettingsRequest.Builder()
                .addLocationRequest(getConfiguration().gpServicesConfiguration().locationRequest())
                .build();

        PendingResult<LocationSettingsResult> settingsResult = LocationServices.SettingsApi
              .checkLocationSettings(googleApiClient, settingsRequest);
        settingsResult.setResultCallback(this);
    }

    @SuppressWarnings("ResourceType")
    private void requestLocationUpdate() {
        LocationServices.FusedLocationApi.requestLocationUpdates(googleApiClient,
              getConfiguration().gpServicesConfiguration().locationRequest(), this);
    }

    private void settingsApiFail(@FailType int failType) {
        if (getConfiguration().gpServicesConfiguration().failOnSettingsApiSuspended()) {
            failed(failType);
        } else {
            LogUtils.logE("Even though settingsApi failed, configuration requires moving on. "
                  + "So requesting location update...");
            if (googleApiClient.isConnected()) {
                requestLocationUpdate();
            } else {
                LogUtils.logE("GoogleApiClient is not connected. Aborting...");
                failed(failType);
            }
        }
    }

    private void failed(@FailType int type) {
        if (getListener() != null) {
            getListener().onLocationFailed(type);
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