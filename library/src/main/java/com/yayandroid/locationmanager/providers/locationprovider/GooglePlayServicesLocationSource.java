package com.yayandroid.locationmanager.providers.locationprovider;

import android.app.Activity;
import android.content.Context;
import android.content.IntentSender.SendIntentException;
import android.location.Location;
import android.os.Looper;

import androidx.annotation.NonNull;

import com.google.android.gms.common.api.ResolvableApiException;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationAvailability;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.location.LocationSettingsResponse;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.yayandroid.locationmanager.constants.RequestCode;

class GooglePlayServicesLocationSource extends LocationCallback implements OnCompleteListener<LocationSettingsResponse> {

    private final FusedLocationProviderClient googleApiClient;
    private final LocationRequest locationRequest;
    private final SourceListener sourceListener;

    interface SourceListener {
        void onConnected();

        void onResult(@NonNull Task<LocationSettingsResponse> resultTask);

        void onLocationChanged(@NonNull Location location);
    }

    GooglePlayServicesLocationSource(Context context, LocationRequest locationRequest, SourceListener sourceListener) {
        this.sourceListener = sourceListener;
        this.locationRequest = locationRequest;
        this.googleApiClient = LocationServices.getFusedLocationProviderClient(context);
    }

    void checkLocationSettings() {
        LocationServices.getSettingsClient(googleApiClient.getApplicationContext())
                .checkLocationSettings(
                        new LocationSettingsRequest.Builder()
                                .addLocationRequest(locationRequest)
                                .build()
                )
                .addOnCompleteListener(this);
    }

    void startSettingsApiResolutionForResult(@NonNull ResolvableApiException resolvable, Activity activity) throws SendIntentException {
        resolvable.startResolutionForResult(activity, RequestCode.SETTINGS_API);
    }

    @SuppressWarnings("ResourceType")
    void requestLocationUpdate() {
        // This method is suited for the foreground use cases
        googleApiClient.requestLocationUpdates(locationRequest, this, Looper.myLooper());
    }

    @NonNull
    Task<Void> removeLocationUpdates() {
        return googleApiClient.removeLocationUpdates(this);
    }

    @SuppressWarnings("ResourceType")
    @NonNull
    Task<LocationAvailability> getLocationAvailability() {
        return googleApiClient.getLocationAvailability();
    }

    @SuppressWarnings("ResourceType")
    @NonNull
    Task<Location> getLastLocation() {
        return googleApiClient.getLastLocation();
    }

    @Override
    public void onComplete(@NonNull Task<LocationSettingsResponse> resultTask) {
        if (sourceListener != null) sourceListener.onResult(resultTask);
    }

    @Override
    public void onLocationResult(LocationResult locationResult) {
        if (locationResult == null) {
            return;
        }

        for (Location location : locationResult.getLocations()) {
            if (sourceListener != null) sourceListener.onLocationChanged(location);
        }
    }

}
