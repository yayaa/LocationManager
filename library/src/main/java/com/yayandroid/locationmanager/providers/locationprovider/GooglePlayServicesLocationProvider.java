package com.yayandroid.locationmanager.providers.locationprovider;

import android.app.Activity;
import android.content.Intent;
import android.content.IntentSender;
import android.location.Location;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.common.api.CommonStatusCodes;
import com.google.android.gms.common.api.ResolvableApiException;
import com.google.android.gms.location.LocationAvailability;
import com.google.android.gms.location.LocationSettingsResponse;
import com.google.android.gms.location.LocationSettingsStatusCodes;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
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

    /**
     * Tracks the status of the location updates request.
     */
    boolean mRequestingLocationUpdates = false;

    private GooglePlayServicesLocationSource googlePlayServicesLocationSource;

    GooglePlayServicesLocationProvider(FallbackListener fallbackListener) {
        this.fallbackListener = new WeakReference<>(fallbackListener);
    }

    @Override
    public void onResume() {
        // not getSourceProvider, because we don't want to connect if it is not already attempt
        if (!settingsDialogIsOn && googlePlayServicesLocationSource != null &&
                (isWaiting() || getConfiguration().keepTracking())) {
            onConnected();
        }
    }

    @Override
    public void onPause() {
        // not getSourceProvider, because we don't want to create if it doesn't already exist
        if (!settingsDialogIsOn && googlePlayServicesLocationSource != null) {
            removeLocationUpdates();
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        // not getSourceProvider, because we don't want to create if it doesn't already exist
        if (googlePlayServicesLocationSource != null) removeLocationUpdates();
    }

    @Override
    public boolean isDialogShowing() {
        return settingsDialogIsOn;
    }

    @Override
    public void get() {
        setWaiting(true);

        if (getContext() != null) {
            onConnected();
        } else {
            failed(FailType.VIEW_DETACHED);
        }
    }

    @Override
    public void cancel() {
        LogUtils.logI("Canceling GooglePlayServiceLocationProvider...");
        // not getSourceProvider, because we don't want to create if it doesn't already exist
        if (googlePlayServicesLocationSource != null) {
            removeLocationUpdates();
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
                mRequestingLocationUpdates = false;
                LogUtils.logI("User denied settingsApi dialog, GooglePlayServices SettingsApi failing...");
                settingsApiFail(FailType.GOOGLE_PLAY_SERVICES_SETTINGS_DENIED);
            }
        }

    }

    @Override
    public void onConnected() {
        LogUtils.logI("Start request location updates.");

        if (mRequestingLocationUpdates) {
            LogUtils.logI("Update already started, wait for result...");

            return;
        }

        mRequestingLocationUpdates = true;

        LogUtils.logI("Request location.");

        if (getConfiguration().googlePlayServicesConfiguration().ignoreLastKnowLocation()) {
            LogUtils.logI("Configuration requires to ignore last know location from GooglePlayServices Api.");

            requestLocation(false);
        } else {
            checkLastKnowLocation();
        }
    }

    @Override
    public void onLocationChanged(@NonNull Location location) {
        if (getListener() != null) {
            getListener().onLocationChanged(location);
        }

        // Set waiting as false because we got at least one, even though we keep tracking user's location
        setWaiting(false);

        if (!getConfiguration().keepTracking()) {
            // If need to update location once, clear the listener to prevent multiple call
            LogUtils.logI("We got location and no need to keep tracking, so location update is removed.");

            removeLocationUpdates();
        }
    }

    @Override
    public void onResult(@NonNull Task<LocationSettingsResponse> resultTask) {

        try {
            //noinspection unused
            LocationSettingsResponse response = resultTask.getResult(ApiException.class);
            // All location settings are satisfied. The client can initialize location
            // requests here.
            LogUtils.logI("We got GPS, Wifi and/or Cell network providers enabled enough "
                    + "to receive location as we needed. Requesting location update...");
            requestLocationUpdate();
        } catch (ApiException exception) {
            switch (exception.getStatusCode()) {
                case LocationSettingsStatusCodes.SETTINGS_CHANGE_UNAVAILABLE:
                    mRequestingLocationUpdates = false;
                    // Location settings are not satisfied.
                    // However, we have no way to fix the settings so we won't show the dialog.
                    LogUtils.logE("Settings change is not available, SettingsApi failing...");
                    settingsApiFail(FailType.GOOGLE_PLAY_SERVICES_SETTINGS_DIALOG);

                    break;
                case LocationSettingsStatusCodes.RESOLUTION_REQUIRED:
                    // Location settings are not satisfied. But could be fixed by showing the user
                    // a dialog.
                    try {
                        // Cast to a resolvable exception.
                        resolveSettingsApi((ResolvableApiException) exception);

                        break;
                    } catch (ClassCastException e) {
                        // Ignore, should be an impossible error.
                    }

                    break;
                default:
                    mRequestingLocationUpdates = false;
                    LogUtils.logE("LocationSettings failing, status: " + CommonStatusCodes.getStatusCodeString(exception.getStatusCode()));
                    settingsApiFail(FailType.GOOGLE_PLAY_SERVICES_SETTINGS_DENIED);

                    break;
            }
        }
    }

    void resolveSettingsApi(@NonNull ResolvableApiException resolvable) {
        try {
            // Show the dialog by calling startResolutionForResult(),
            // and check the result in onActivityResult().
            LogUtils.logI("We need settingsApi dialog to switch required settings on.");
            if (getActivity() != null) {
                LogUtils.logI("Displaying the dialog...");
                getSourceProvider().startSettingsApiResolutionForResult(resolvable, getActivity());
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

    void checkLastKnowLocation() {
        getSourceProvider().getLocationAvailability()
                .addOnSuccessListener(new OnSuccessListener<LocationAvailability>() {
                    /**
                     * Returns the availability of location data. When isLocationAvailable() returns true, then the location returned by getLastLocation() will be reasonably up to date within the hints specified by the active LocationRequests.
                     *
                     * If the client isn't connected to Google Play services and the request times out, null is returned.
                     *
                     * Note it's always possible for getLastLocation() to return null even when this method returns true (e.g. location settings were disabled between calls).
                     */
                    @Override
                    public void onSuccess(@Nullable LocationAvailability locationAvailability) {
                        if (locationAvailability != null && locationAvailability.isLocationAvailable()) {
                            getSourceProvider().getLastLocation()
                                    .addOnCompleteListener(new OnCompleteListener<Location>() {
                                        @Override
                                        public void onComplete(@NonNull Task<Location> task) {
                                            /*
                                             * Returns the best most recent location currently available.
                                             *
                                             * If a location is not available, which should happen very rarely, null will be returned. The best accuracy available while respecting the location permissions will be returned.
                                             *
                                             * This method provides a simplified way to get location. It is particularly well suited for applications that do not require an accurate location and that do not want to maintain extra logic for location updates.
                                             */
                                            if (task.isSuccessful() && task.getResult() != null) {
                                                Location lastKnownLocation = task.getResult();

                                                // GPS location can be null if GPS is switched off
                                                if (lastKnownLocation != null) {
                                                    LogUtils.logI("LastKnowLocation is available.");
                                                    onLocationChanged(lastKnownLocation);

                                                    requestLocation(true);
                                                } else {
                                                    LogUtils.logI("LastKnowLocation is not available.");

                                                    requestLocation(false);
                                                }
                                            } else {
                                                LogUtils.logI("LastKnowLocation is not available.");

                                                requestLocation(false);
                                            }
                                        }
                                    });

                        } else {
                            LogUtils.logI("LastKnowLocation is not available.");

                            requestLocation(false);
                        }
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        LogUtils.logI("LastKnowLocation is not available.");

                        requestLocation(false);
                    }
                });
    }

    void requestLocation(boolean locationIsAlreadyAvailable) {
        if (getConfiguration().keepTracking() || !locationIsAlreadyAvailable) {
            locationRequired();
        } else {
            LogUtils.logI("We got location, no need to ask for location updates.");
        }
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

        LogUtils.logI("Requesting location update...");
        mRequestingLocationUpdates = true;
        getSourceProvider().requestLocationUpdate();
    }

    void settingsApiFail(@FailType int failType) {
        if (getConfiguration().googlePlayServicesConfiguration().failOnSettingsApiSuspended()) {
            failed(failType);
        } else {
            LogUtils.logE("Even though settingsApi failed, configuration requires moving on. "
                  + "So requesting location update...");

            requestLocationUpdate();
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

    private void removeLocationUpdates() {
        LogUtils.logI("Stop location updates...");

        // not getSourceProvider, because we don't want to create if it doesn't already exist
        if (googlePlayServicesLocationSource != null) {
            googlePlayServicesLocationSource.removeLocationUpdates()
                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            mRequestingLocationUpdates = false;
                        }
                    });
        }
    }

}