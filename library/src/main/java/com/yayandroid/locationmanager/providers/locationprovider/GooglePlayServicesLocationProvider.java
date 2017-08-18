package com.yayandroid.locationmanager.providers.locationprovider;

import android.app.Activity;
import android.content.Intent;
import android.content.IntentSender;
import android.location.Location;
import android.os.Bundle;
import android.support.annotation.NonNull;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.LocationSettingsResult;
import com.google.android.gms.location.LocationSettingsStatusCodes;
import com.yayandroid.locationmanager.constants.FailType;
import com.yayandroid.locationmanager.constants.ProcessType;
import com.yayandroid.locationmanager.constants.RequestCode;
import com.yayandroid.locationmanager.helper.LogUtils;
import com.yayandroid.locationmanager.listener.FallbackListener;
import com.yayandroid.locationmanager.providers.locationprovider.GooglePlayServicesLocationSource.SourceListener;

import java.lang.ref.WeakReference;

public class GooglePlayServicesLocationProvider extends LocationProvider implements SourceListener {

    private final WeakReference<FallbackListener> fallbackListener;

    private boolean settingsDialogIsOn = false;
    private boolean waitingForConnectionToRequestLocationUpdate = true;
    private int suspendedConnectionIteration = 0;
    private GooglePlayServicesLocationSource googlePlayServicesLocationSource;

    GooglePlayServicesLocationProvider(FallbackListener fallbackListener) {
        this.fallbackListener = new WeakReference<>(fallbackListener);
    }

    @Override
    public void onResume() {
        // not getSourceProvider, because we don't want to connect if it is not already attempt
        if (!settingsDialogIsOn && googlePlayServicesLocationSource != null &&
                (isWaiting() || getConfiguration().keepTracking())) {
            googlePlayServicesLocationSource.connectGoogleApiClient();
        }
    }

    @Override
    public void onPause() {
        // not getSourceProvider, because we don't want to create if it doesn't already exist
        if (!settingsDialogIsOn && googlePlayServicesLocationSource != null
              && googlePlayServicesLocationSource.isGoogleApiClientConnected()) {
            googlePlayServicesLocationSource.disconnectGoogleApiClient();
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        // not getSourceProvider, because we don't want to create if it doesn't already exist
        if (googlePlayServicesLocationSource != null) googlePlayServicesLocationSource.clearGoogleApiClient();
    }

    @Override
    public boolean isDialogShowing() {
        return settingsDialogIsOn;
    }

    @Override
    public void get() {
        setWaiting(true);

        if (getContext() != null) {
            getSourceProvider().connectGoogleApiClient();
        } else {
            failed(FailType.VIEW_DETACHED);
        }
    }

    @Override
    public void cancel() {
        LogUtils.logI("Canceling GooglePlayServiceLocationProvider...");
        // not getSourceProvider, because we don't want to create if it doesn't already exist
        if (googlePlayServicesLocationSource != null &&
              googlePlayServicesLocationSource.isGoogleApiClientConnected()) {
            googlePlayServicesLocationSource.removeLocationUpdates();
            googlePlayServicesLocationSource.disconnectGoogleApiClient();
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
                LogUtils.logI("User denied settingsApi dialog, GooglePlayServices SettingsApi failing...");
                settingsApiFail(FailType.GOOGLE_PLAY_SERVICES_SETTINGS_DENIED);
            }
        }

    }

    @Override
    public void onConnected(Bundle bundle) {
        LogUtils.logI("GoogleApiClient is connected.");
        boolean locationIsAlreadyAvailable = false;

        if (getConfiguration().googlePlayServicesConfiguration().ignoreLastKnowLocation()) {
            LogUtils.logI("Configuration requires to ignore last know location from GooglePlayServices Api.");
        } else {
            locationIsAlreadyAvailable = checkLastKnowLocation();
        }

        if (getConfiguration().keepTracking() || !locationIsAlreadyAvailable
              || waitingForConnectionToRequestLocationUpdate) {
            waitingForConnectionToRequestLocationUpdate(false);
            locationRequired();
        } else {
            LogUtils.logI("We got location, no need to ask for location updates.");
        }
    }

    @Override
    public void onConnectionSuspended(int i) {
        if (!getConfiguration().googlePlayServicesConfiguration().failOnConnectionSuspended()
              && suspendedConnectionIteration < getConfiguration().googlePlayServicesConfiguration()
              .suspendedConnectionRetryCount()) {
            LogUtils.logI("GoogleApiClient connection is suspended, try to connect again.");
            suspendedConnectionIteration++;
            getSourceProvider().connectGoogleApiClient();
        } else {
            LogUtils.logI("GoogleApiClient connection is suspended, calling fail...");
            failed(FailType.GOOGLE_PLAY_SERVICES_CONNECTION_FAIL);
        }
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
        LogUtils.logI("GoogleApiClient connection is failed.");
        failed(FailType.GOOGLE_PLAY_SERVICES_CONNECTION_FAIL);
    }

    @Override
    public void onLocationChanged(Location location) {
        if (getListener() != null) {
            getListener().onLocationChanged(location);
        }

        // Set waiting as false because we got at least one, even though we keep tracking user's location
        setWaiting(false);

        if (!getConfiguration().keepTracking() && getSourceProvider().isGoogleApiClientConnected()) {
            LogUtils.logI("We got location and no need to keep tracking, so location update is removed.");
            getSourceProvider().removeLocationUpdates();
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
            case LocationSettingsStatusCodes.SETTINGS_CHANGE_UNAVAILABLE:
                // Location settings are not satisfied.
                // However, we have no way to fix the settings so we won't show the dialog.
                LogUtils.logE("Settings change is not available, SettingsApi failing...");
                settingsApiFail(FailType.GOOGLE_PLAY_SERVICES_SETTINGS_DIALOG);
                break;
            case LocationSettingsStatusCodes.RESOLUTION_REQUIRED:
                // Location settings are not satisfied. But could be fixed by showing the user
                // a dialog.
                resolveSettingsApi(status);
                break;
        }
    }

    void resolveSettingsApi(Status status) {
        try {
            // Show the dialog by calling startResolutionForResult(),
            // and check the result in onActivityResult().
            LogUtils.logI("We need settingsApi dialog to switch required settings on.");
            if (getActivity() != null) {
                LogUtils.logI("Displaying the dialog...");
                getSourceProvider().startSettingsApiResolutionForResult(status, getActivity());
                settingsDialogIsOn = true;
            } else {
                LogUtils.logI("Settings Api cannot show dialog if LocationManager is not running on an activity!");
                settingsApiFail(FailType.VIEW_NOT_REQUIRED_TYPE);
            }
        } catch (IntentSender.SendIntentException e) {
            LogUtils.logE("Error on displaying SettingsApi dialog, SettingsApi failing...");
            settingsApiFail(FailType.GOOGLE_PLAY_SERVICES_SETTINGS_DIALOG);
        }
    }

    boolean checkLastKnowLocation() {
        if (getSourceProvider().getLocationAvailability()) {
            Location lastKnownLocation = getSourceProvider().getLastLocation();
            if (lastKnownLocation != null) {
                LogUtils.logI("LastKnowLocation is available.");
                onLocationChanged(lastKnownLocation);
                return true;
            } else {
                LogUtils.logI("LastKnowLocation is not available.");
            }
        } else {
            LogUtils.logI("LastKnowLocation is not available.");
        }
        return false;
    }

    void locationRequired() {
        LogUtils.logI("Ask for location update...");
        if (getConfiguration().googlePlayServicesConfiguration().askForSettingsApi()) {
            LogUtils.logI("Asking for SettingsApi...");
            getSourceProvider().checkLocationSettings();
        } else {
            LogUtils.logI("SettingsApi is not enabled, requesting for location update...");
            requestLocationUpdate();
        }
    }

    void requestLocationUpdate() {
        if (getListener() != null) {
            getListener().onProcessTypeChanged(ProcessType.GETTING_LOCATION_FROM_GOOGLE_PLAY_SERVICES);
        }

        if (getSourceProvider().isGoogleApiClientConnected()) {
            LogUtils.logI("Requesting location update...");
            getSourceProvider().requestLocationUpdate();
        } else {
            LogUtils.logI("Tried to requestLocationUpdate, but GoogleApiClient wasn't connected. Trying to connect...");
            waitingForConnectionToRequestLocationUpdate(true);
            getSourceProvider().connectGoogleApiClient();
        }
    }

    void settingsApiFail(@FailType int failType) {
        if (getConfiguration().googlePlayServicesConfiguration().failOnSettingsApiSuspended()) {
            failed(failType);
        } else {
            LogUtils.logE("Even though settingsApi failed, configuration requires moving on. "
                  + "So requesting location update...");
            if (getSourceProvider().isGoogleApiClientConnected()) {
                requestLocationUpdate();
            } else {
                LogUtils.logE("GoogleApiClient is not connected. Aborting...");
                failed(failType);
            }
        }
    }

    void failed(@FailType int type) {
        if (getConfiguration().googlePlayServicesConfiguration().fallbackToDefault() && fallbackListener.get() != null) {
            fallbackListener.get().onFallback();
        } else {
            if (getListener() != null) {
                getListener().onLocationFailed(type);
            }
        }
        setWaiting(false);
    }

    void waitingForConnectionToRequestLocationUpdate(boolean isWaiting) {
        waitingForConnectionToRequestLocationUpdate = isWaiting;
    }

    // For test purposes
    void setDispatcherLocationSource(GooglePlayServicesLocationSource googlePlayServicesLocationSource) {
        this.googlePlayServicesLocationSource = googlePlayServicesLocationSource;
    }

    private GooglePlayServicesLocationSource getSourceProvider() {
        if (googlePlayServicesLocationSource == null) {
            googlePlayServicesLocationSource = new GooglePlayServicesLocationSource(getContext(),
                  getConfiguration().googlePlayServicesConfiguration().locationRequest(), this);
        }
        return googlePlayServicesLocationSource;
    }

}