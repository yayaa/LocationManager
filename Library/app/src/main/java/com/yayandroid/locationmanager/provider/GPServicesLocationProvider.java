package com.yayandroid.locationmanager.provider;

import android.location.Location;
import android.os.Bundle;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationServices;
import com.yayandroid.locationmanager.constants.FailType;
import com.yayandroid.locationmanager.constants.LogType;
import com.yayandroid.locationmanager.helper.LocationUtils;
import com.yayandroid.locationmanager.helper.LogUtils;

/**
 * Created by Yahya Bayramoglu on 09/02/16.
 */
public class GPServicesLocationProvider extends LocationProvider implements LocationListener, GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener {

    private GoogleApiClient googleApiClient;

    @Override
    public void onResume() {
        if (googleApiClient != null) {
            googleApiClient.connect();
        }
    }

    @Override
    public void onPause() {
        if (googleApiClient != null && googleApiClient.isConnected()) {
            googleApiClient.disconnect();
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

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

    @Override
    public boolean requiresActivityResult() {
        return false;
    }

    @Override
    public boolean isDialogShowing() {
        return false;
    }

    @Override
    public void get() {
        setWaiting(true);

        googleApiClient = new GoogleApiClient.Builder(activity)
                .addApi(LocationServices.API)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .build();
        googleApiClient.connect();
    }

    @Override
    public void cancel() {
        LogUtils.logI("Canceling GPServiceLocationProvider...", LogType.GENERAL);
        if (googleApiClient != null && googleApiClient.isConnected()) {
            LocationServices.FusedLocationApi.removeLocationUpdates(googleApiClient, this);
            googleApiClient.disconnect();
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

        if (configuration.shouldKeepTracking() || !locationIsAlreadyAvailable) {
            LogUtils.logI("Ask for location update...", LogType.IMPORTANT);
            LocationServices.FusedLocationApi.requestLocationUpdates(googleApiClient, configuration.getLocationRequest(), this);
        } else {
            LogUtils.logI("We got location, no need to ask for location updates.", LogType.GENERAL);
        }
    }

    @Override
    public void onConnectionSuspended(int i) {
        if (!configuration.shouldFailWhenSuspended() && googleApiClient != null) {
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

        if (!configuration.shouldKeepTracking()) {
            LogUtils.logI("We got location and no need to keep tracking, so location update is removed.", LogType.GENERAL);
            LocationServices.FusedLocationApi.removeLocationUpdates(googleApiClient, this);
        }
    }

    private void failed(int type) {
        if (listener != null) {
            listener.onLocationFailed(type);
        }
        setWaiting(false);
    }

}