package com.yayandroid.locationmanager.configuration;

import com.yayandroid.locationmanager.constants.Default;

public final class LocationConfiguration {

    private final String rationalMessage;
    private final String[] requiredPermissions;
    private final long requiredTimeInterval;
    private final boolean keepTracking;
    private final GPServicesConfiguration gpServicesConfiguration;
    private final DefaultProviderConfiguration defaultProviderConfiguration;

    private LocationConfiguration(Builder builder) {
        this.rationalMessage = builder.rationalMessage;
        this.requiredPermissions = builder.requiredPermissions;
        this.keepTracking = builder.keepTracking;
        this.requiredTimeInterval = builder.requiredTimeInterval;
        this.gpServicesConfiguration = builder.gpServicesConfiguration;
        this.defaultProviderConfiguration = builder.defaultProviderConfiguration;
    }

    // region Getters
    public GPServicesConfiguration gpServicesConfiguration() {
        return gpServicesConfiguration;
    }

    public DefaultProviderConfiguration defaultProviderConfiguration() {
        return defaultProviderConfiguration;
    }

    public boolean keepTracking() {
        return keepTracking;
    }

    public String rationalMessage() {
        return rationalMessage;
    }

    public String[] requiredPermissions() {
        return requiredPermissions;
    }

    public long requiredTimeInterval() {
        return requiredTimeInterval;
    }
    // endregion

    public static class Builder {

        private String rationalMessage = ConfigurationDefaults.EMPTY_STRING;
        private String[] requiredPermissions = ConfigurationDefaults.LOCATION_PERMISSIONS;
        private long requiredTimeInterval = ConfigurationDefaults.LOCATION_INTERVAL;
        private boolean keepTracking = ConfigurationDefaults.KEEP_TRACKING;
        private GPServicesConfiguration gpServicesConfiguration;
        private DefaultProviderConfiguration defaultProviderConfiguration;

        /**
         * If you need to keep receiving location updates, then you need to set this as true.
         * Otherwise manager will be aborted after any location received.
         * <p>
         * Default is False.
         */
        public Builder keepTracking(boolean keepTracking) {
            this.keepTracking = keepTracking;
            return this;
        }

        /**
         * Indicates what to display when user needs to see a rational dialog for RuntimePermission.
         * There is no default value, so if you do not set this it will create an empty dialog.
         */
        public Builder rationalMessage(String rationalMessage) {
            this.rationalMessage = rationalMessage;
            return this;
        }

        /**
         * If you need to ask any other permissions beside {@linkplain Default#LOCATION_PERMISSIONS}
         * or you may not need both of those permissions, you can change permissions
         * by calling this method with new permissions' array.
         */
        public Builder requiredPermissions(String[] permissions) {
            this.requiredPermissions = permissions;
            return this;
        }

        /**
         * TimeInterval will be used while getting location from default location providers
         * It will define in which period updates need to be delivered
         * <p>
         * Default is {@linkplain Default#LOCATION_INTERVAL}
         */
        public Builder requiredTimeInterval(long requiredTimeInterval) {
            if (requiredTimeInterval < 0)
                throw new IllegalArgumentException("Required time interval cannot be set to minus values.");

            this.requiredTimeInterval = requiredTimeInterval;
            return this;
        }

        /**
         * In order to configure GooglePlayServices Api, if this is not set, then GooglePlayServices will not be used.
         */
        public Builder useGooglePlayServices(GPServicesConfiguration gpServicesConfiguration) {
            this.gpServicesConfiguration = gpServicesConfiguration;
            return this;
        }

        /**
         * In order to configure Default Location Providers, if this is not set, then they will not be used.
         */
        public Builder useDefaultProviders(DefaultProviderConfiguration defaultProviderConfiguration) {
            this.defaultProviderConfiguration = defaultProviderConfiguration;
            return this;
        }

        public LocationConfiguration build() {
            if (gpServicesConfiguration == null && defaultProviderConfiguration == null) {
                throw new IllegalStateException("You need to specify one of the provider configurations."
                      + " Please see GPServicesConfiguration and DefaultProviderConfiguration");
            }

            if (requiredPermissions == null || requiredPermissions.length == 0) {
                throw new IllegalStateException("Required Permissions cannot be empty."
                      + " If you don't set anything special library will ask for"
                      + " ACCESS_FINE_LOCATION and ACCESS_COARSE_LOCATION");
            }

            return new LocationConfiguration(this);
        }

    }
}
