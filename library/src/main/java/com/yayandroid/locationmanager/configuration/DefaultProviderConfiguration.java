package com.yayandroid.locationmanager.configuration;

import android.text.TextUtils;

import com.yayandroid.locationmanager.constants.ProviderType;
import com.yayandroid.locationmanager.providers.dialogprovider.DialogProvider;
import com.yayandroid.locationmanager.providers.dialogprovider.SimpleMessageDialogProvider;

public final class DefaultProviderConfiguration {

    private final long requiredTimeInterval;
    private final long requiredDistanceInterval;
    private final float acceptableAccuracy;
    private final long acceptableTimePeriod;
    private final long gpsWaitPeriod;
    private final long networkWaitPeriod;
    private final DialogProvider gpsDialogProvider;

    private DefaultProviderConfiguration(Builder builder) {
        this.requiredTimeInterval = builder.requiredTimeInterval;
        this.requiredDistanceInterval = builder.requiredDistanceInterval;
        this.acceptableAccuracy = builder.acceptableAccuracy;
        this.acceptableTimePeriod = builder.acceptableTimePeriod;
        this.gpsWaitPeriod = builder.gpsWaitPeriod;
        this.networkWaitPeriod = builder.networkWaitPeriod;
        this.gpsDialogProvider = builder.gpsDialogProvider;
    }

    public DefaultProviderConfiguration.Builder newBuilder() {
        return new DefaultProviderConfiguration.Builder()
              .requiredTimeInterval(requiredTimeInterval)
              .requiredDistanceInterval(requiredDistanceInterval)
              .acceptableAccuracy(acceptableAccuracy)
              .acceptableTimePeriod(acceptableTimePeriod)
              .setWaitPeriod(ProviderType.GPS, gpsWaitPeriod)
              .setWaitPeriod(ProviderType.NETWORK, networkWaitPeriod)
              .gpsDialogProvider(gpsDialogProvider);
    }

    // region Getters
    public long requiredTimeInterval() {
        return requiredTimeInterval;
    }

    public long requiredDistanceInterval() {
        return requiredDistanceInterval;
    }

    public float acceptableAccuracy() {
        return acceptableAccuracy;
    }

    public long acceptableTimePeriod() {
        return acceptableTimePeriod;
    }

    public boolean askForGPSEnable() {
        return gpsDialogProvider != null;
    }

    public DialogProvider getGpsDialogProvider() {
        return gpsDialogProvider;
    }

    public long gpsWaitPeriod() {
        return gpsWaitPeriod;
    }

    public long networkWaitPeriod() {
        return networkWaitPeriod;
    }
    // endregion


    public static class Builder {

        private long requiredTimeInterval = Defaults.LOCATION_INTERVAL;
        private long requiredDistanceInterval = Defaults.LOCATION_DISTANCE_INTERVAL;
        private float acceptableAccuracy = Defaults.MIN_ACCURACY;
        private long acceptableTimePeriod = Defaults.TIME_PERIOD;
        private long gpsWaitPeriod = Defaults.WAIT_PERIOD;
        private long networkWaitPeriod = Defaults.WAIT_PERIOD;
        private DialogProvider gpsDialogProvider;
        private String gpsMessage = Defaults.EMPTY_STRING;

        /**
         * TimeInterval will be used while getting location from default location providers
         * It will define in which period updates need to be delivered and will be used only when
         * {@linkplain LocationConfiguration#keepTracking} is set to true.
         * Default is {@linkplain Defaults#LOCATION_INTERVAL}
         */
        public Builder requiredTimeInterval(long requiredTimeInterval) {
            this.requiredTimeInterval = requiredTimeInterval;
            return this;
        }

        /**
         * DistanceInterval will be used while getting location from default location providers
         * It will define in which distance changes that we should receive an update and will be used only when
         * {@linkplain LocationConfiguration#keepTracking} is set to true.
         * Default is {@linkplain Defaults#LOCATION_DISTANCE_INTERVAL}
         */
        public Builder requiredDistanceInterval(long requiredDistanceInterval) {
            this.requiredDistanceInterval = requiredDistanceInterval;
            return this;
        }

        /**
         * Minimum Accuracy that you seek location to be
         * Default is {@linkplain Defaults#MIN_ACCURACY}
         */
        public Builder acceptableAccuracy(float acceptableAccuracy) {
            this.acceptableAccuracy = acceptableAccuracy;
            return this;
        }

        /**
         * Indicates time period that can be count as usable location,
         * this needs to be considered such as "last 5 minutes"
         * Default is {@linkplain Defaults#TIME_PERIOD}
         */
        public Builder acceptableTimePeriod(long acceptableTimePeriod) {
            this.acceptableTimePeriod = acceptableTimePeriod;
            return this;
        }

        /**
         * Indicates what to display to user while asking to turn GPS on.
         * If you do not set this, user will not be asked to enable GPS.
         */
        public Builder gpsMessage(String gpsMessage) {
            this.gpsMessage = gpsMessage;
            return this;
        }

        /**
         * If you need to display a custom dialog to ask user to enable GPS, you can provide your own
         * implementation of {@linkplain DialogProvider} and manager will use that implementation to display the dialog.
         * Important, if you set your own implementation, please make sure to handle gpsMessage as well.
         * Because {@linkplain DefaultProviderConfiguration.Builder#gpsMessage} will be ignored in that case.
         *
         * If you don't specify any dialogProvider implementation {@linkplain SimpleMessageDialogProvider} will be used with
         * given {@linkplain DefaultProviderConfiguration.Builder#gpsMessage}
         */
        public Builder gpsDialogProvider(DialogProvider dialogProvider) {
            this.gpsDialogProvider = dialogProvider;
            return this;
        }

        /**
         * Indicates waiting time period before switching to next possible provider.
         * Possible to set provider wait periods separately by passing providerType as one of the
         * {@linkplain ProviderType} values.
         * Default values are {@linkplain Defaults#WAIT_PERIOD}
         */
        public Builder setWaitPeriod(@ProviderType int providerType, long milliseconds) {
            switch (providerType) {
                case ProviderType.GOOGLE_PLAY_SERVICES: {
                    throw new IllegalStateException("GooglePlayServices waiting time period should be set on "
                          + "GPServicesConfiguration");
                }
                case ProviderType.NETWORK: {
                    this.networkWaitPeriod = milliseconds;
                    break;
                }
                case ProviderType.GPS: {
                    this.gpsWaitPeriod = milliseconds;
                    break;
                }
                case ProviderType.DEFAULT_PROVIDERS: {
                    this.gpsWaitPeriod = milliseconds;
                    this.networkWaitPeriod = milliseconds;
                    break;
                }
                case ProviderType.NONE: {
                    // ignored
                }
            }
            return this;
        }

        public DefaultProviderConfiguration build() {
            if (requiredTimeInterval < 0) {
                throw new IllegalArgumentException("Required time interval cannot be set to negative value.");
            }

            if (requiredDistanceInterval < 0) {
                throw new IllegalArgumentException("Required distance interval cannot be set to negative value.");
            }

            if (acceptableAccuracy < 0) {
                throw new IllegalArgumentException("Acceptable accuracy cannot be set to negative value.");
            }

            if (acceptableTimePeriod < 0) {
                throw new IllegalArgumentException("Acceptable time period cannot be set to negative value.");
            }

            if (networkWaitPeriod < 0) {
                throw new IllegalArgumentException("Network Wait period cannot be set to negative value.");
            }

            if (gpsWaitPeriod < 0) {
                throw new IllegalArgumentException("GPS Wait period cannot be set to negative value.");
            }

            if (gpsDialogProvider == null && !TextUtils.isEmpty(gpsMessage)) {
                gpsDialogProvider = new SimpleMessageDialogProvider(gpsMessage);
            }

            return new DefaultProviderConfiguration(this);
        }
    }
}
