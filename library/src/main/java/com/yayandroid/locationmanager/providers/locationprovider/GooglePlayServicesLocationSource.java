package com.yayandroid.locationmanager.providers.locationprovider;

import android.app.Activity;
import android.content.Context;
import android.content.IntentSender.SendIntentException;
import android.location.Location;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.GoogleApiClient.Builder;
import com.google.android.gms.common.api.GoogleApiClient.ConnectionCallbacks;
import com.google.android.gms.common.api.GoogleApiClient.OnConnectionFailedListener;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.LocationAvailability;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.location.LocationSettingsResult;
import com.yayandroid.locationmanager.constants.RequestCode;

class GooglePlayServicesLocationSource implements LocationListener, ConnectionCallbacks, OnConnectionFailedListener,
      ResultCallback<LocationSettingsResult> {

    private final GoogleApiClient googleApiClient;
    private final LocationRequest locationRequest;
    private final SourceListener sourceListener;

    interface SourceListener {
        void onConnected(Bundle bundle);

        void onConnectionSuspended(int i);

        void onConnectionFailed(@NonNull ConnectionResult connectionResult);

        void onResult(@NonNull LocationSettingsResult result);

        void onLocationChanged(Location location);
    }

    GooglePlayServicesLocationSource(Context context, LocationRequest locationRequest, SourceListener sourceListener) {
        this.sourceListener = sourceListener;
        this.locationRequest = locationRequest;
        this.googleApiClient = new Builder(context)
              .addApi(LocationServices.API)
              .addConnectionCallbacks(this)
              .addOnConnectionFailedListener(this)
              .build();
    }

    boolean isGoogleApiClientConnected() {
        return googleApiClient.isConnected();
    }

    void connectGoogleApiClient() {
        googleApiClient.connect();
    }

    void disconnectGoogleApiClient() {
        googleApiClient.disconnect();
    }

    void clearGoogleApiClient() {
        googleApiClient.unregisterConnectionCallbacks(this);
        googleApiClient.unregisterConnectionFailedListener(this);

        if (googleApiClient.isConnected()) {
            removeLocationUpdates();
        }

        googleApiClient.disconnect();
    }

    void checkLocationSettings() {
        LocationServices.SettingsApi
              .checkLocationSettings(googleApiClient, new LocationSettingsRequest.Builder()
                    .addLocationRequest(locationRequest)
                    .build())
              .setResultCallback(this);
    }

    void startSettingsApiResolutionForResult(Status status, Activity activity) throws SendIntentException {
        status.startResolutionForResult(activity, RequestCode.SETTINGS_API);
    }

    @SuppressWarnings("ResourceType")
    void requestLocationUpdate() {
        LocationServices.FusedLocationApi.requestLocationUpdates(googleApiClient, locationRequest, this);
    }

    void removeLocationUpdates() {
        LocationServices.FusedLocationApi.removeLocationUpdates(googleApiClient, this);
    }

    @SuppressWarnings("ResourceType")
    boolean getLocationAvailability() {
        LocationAvailability locationAvailability =
              LocationServices.FusedLocationApi.getLocationAvailability(googleApiClient);
        return locationAvailability != null && locationAvailability.isLocationAvailable();
    }

    @SuppressWarnings("ResourceType")
    Location getLastLocation() {
        return LocationServices.FusedLocationApi.getLastLocation(googleApiClient);
    }

    @Override
    public void onConnected(@Nullable Bundle bundle) {
        if (sourceListener != null) sourceListener.onConnected(bundle);
    }

    @Override
    public void onConnectionSuspended(int i) {
        if (sourceListener != null) sourceListener.onConnectionSuspended(i);
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
        if (sourceListener != null) sourceListener.onConnectionFailed(connectionResult);
    }

    @Override
    public void onResult(@NonNull LocationSettingsResult locationSettingsResult) {
        if (sourceListener != null) sourceListener.onResult(locationSettingsResult);
    }

    @Override
    public void onLocationChanged(Location location) {
        if (sourceListener != null) sourceListener.onLocationChanged(location);
    }
}
