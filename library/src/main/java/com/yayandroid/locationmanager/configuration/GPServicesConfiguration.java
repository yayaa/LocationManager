package com.yayandroid.locationmanager.configuration;

import android.support.annotation.NonNull;

import com.google.android.gms.location.LocationRequest;
import com.yayandroid.locationmanager.constants.Default;

public final class GPServicesConfiguration {

    private final LocationRequest locationRequest;
    private final boolean askForGPServices;
    private final boolean askForSettingsApi;
    private final boolean failOnConnectionSuspended;
    private final boolean failOnSettingsApiSuspended;
    private final long gpServicesWaitPeriod;

    private GPServicesConfiguration(Builder builder) {
        this.locationRequest = builder.locationRequest;
        this.askForGPServices = builder.askForGPServices;
        this.askForSettingsApi = builder.askForSettingsApi;
        this.failOnConnectionSuspended = builder.failOnConnectionSuspended;
        this.failOnSettingsApiSuspended = builder.failOnSettingsApiSuspended;
        this.gpServicesWaitPeriod = builder.gpServicesWaitPeriod;
    }

    // region Getters
    public LocationRequest locationRequest() {
        return locationRequest;
    }

    public boolean askForGPServices() {
        return askForGPServices;
    }

    public boolean askForSettingsApi() {
        return askForSettingsApi;
    }

    public boolean failOnConnectionSuspended() {
        return failOnConnectionSuspended;
    }

    public boolean failOnSettingsApiSuspended() {
        return failOnSettingsApiSuspended;
    }

    public long gpServicesWaitPeriod() {
        return gpServicesWaitPeriod;
    }
    // endregion

    public static class Builder {

        private LocationRequest locationRequest = generateDefaultLocationRequest();
        private boolean askForGPServices = ConfigurationDefaults.ASK_FOR_GP_SERVICES;
        private boolean askForSettingsApi = ConfigurationDefaults.ASK_FOR_SETTINGS_API;
        private boolean failOnConnectionSuspended = ConfigurationDefaults.FAIL_ON_CONNECTION_SUSPENDED;
        private boolean failOnSettingsApiSuspended = ConfigurationDefaults.FAIL_ON_SETTINGS_API_SUSPENDED;
        private long gpServicesWaitPeriod = ConfigurationDefaults.WAIT_PERIOD;

        /**
         * https://developers.google.com/android/reference/com/google/android/gms/location/LocationRequest
         */
        private LocationRequest generateDefaultLocationRequest() {
            return LocationRequest.create()
                  .setPriority(ConfigurationDefaults.LOCATION_PRIORITY)
                  .setInterval(ConfigurationDefaults.LOCATION_INTERVAL)
                  .setFastestInterval(ConfigurationDefaults.LOCATION_FASTEST_INTERVAL);
        }

        /**
         * Set true to ask user handle when there is some resolvable error
         * on connection GooglePlayServices, if you don't want to bother user
         * to configure Google Play Services to receive location then set this as false.
         * <p>
         * Default is False.
         */
        public Builder askForGPServices(boolean askForGPServices) {
            this.askForGPServices = askForGPServices;
            return this;
        }

        /**
         * While trying to get location via GooglePlayServices LocationApi,
         * manager will check whether GPS, Wifi and Cell networks are available or not.
         * Then if this flag is on it will ask user to turn them on, again, via GooglePlayServices
         * by displaying a system dialog if not it will directly try to receive location
         * -which probably not going to return no values.
         * <p>
         * Default is True.
         */
        public Builder askForSettingsApi(boolean askForSettingsApi) {
            this.askForSettingsApi = askForSettingsApi;
            return this;
        }

        /**
         * As it is described in official documentation when Google Play Services is disconnected,
         * it will call ConnectionSuspended and after some time it will try to reconnect
         * you can determine to fail in this situation or you may want to wait.
         * <p>
         * Default is True.
         * <p>
         * https://developers.google.com/android/reference/com/google/android/gms/common/api/GoogleApiClient
         * .ConnectionCallbacks#onConnectionSuspended(int)
         */
        public Builder failOnConnectionSuspended(boolean failOnConnectionSuspended) {
            this.failOnConnectionSuspended = failOnConnectionSuspended;
            return this;
        }

        /**
         * This flag will be checked when it is not possible to display user a settingsApi dialog
         * to switch necessary providers on, or when there is an error displaying the dialog.
         * If the flag is on, then manager will notify listener as location failed,
         * otherwise it will try to get location anyway -which probably not gonna happen.
         * <p>
         * Default is False. -Because after GooglePlayServices Provider it might switch
         * to default providers, if we fail here then those provider will never trigger.
         */
        public Builder failOnSettingsApiSuspended(boolean failOnSettingsApiSuspended) {
            this.failOnSettingsApiSuspended = failOnSettingsApiSuspended;
            return this;
        }

        /**
         * LocationRequest object that you specified to use while getting location from Google Play Services
         * <p>
         * Default is {@linkplain Builder#generateDefaultLocationRequest()}
         */
        public Builder locationRequest(@NonNull LocationRequest locationRequest) {
            this.locationRequest = locationRequest;
            return this;
        }

        /**
         * Indicates waiting time period for GooglePlayServices before switching to next possible provider.
         * <p>
         * Default values are {@linkplain Default#WAIT_PERIOD}
         */
        public Builder setWaitPeriod(long milliseconds) {
            if (milliseconds < 0)
                throw new IllegalArgumentException("Wait period cannot be set to minus values.");

            this.gpServicesWaitPeriod = milliseconds;
            return this;
        }

        public GPServicesConfiguration build() {
            if (locationRequest == null)
                throw new IllegalStateException("LocationRequest cannot be null.");

            return new GPServicesConfiguration(this);
        }
    }
}
