package com.yayandroid.locationmanager.configuration;

import android.text.TextUtils;

import com.yayandroid.locationmanager.constants.Default;
import com.yayandroid.locationmanager.constants.ProviderType;

public final class DefaultProviderConfiguration {

    private final float acceptableAccuracy;
    private final long acceptableTimePeriod;
    private final long gpsWaitPeriod;
    private final long networkWaitPeriod;
    private final String gpsMessage;

    private DefaultProviderConfiguration(Builder builder) {
        this.acceptableAccuracy = builder.acceptableAccuracy;
        this.acceptableTimePeriod = builder.acceptableTimePeriod;
        this.gpsWaitPeriod = builder.gpsWaitPeriod;
        this.networkWaitPeriod = builder.networkWaitPeriod;
        this.gpsMessage = builder.gpsMessage;
    }

    // region Getters
    public float acceptableAccuracy() {
        return acceptableAccuracy;
    }

    public long acceptableTimePeriod() {
        return acceptableTimePeriod;
    }

    public boolean askForGPSEnable() {
        return gpsMessage != null && gpsMessage.length() > 0;
    }

    public String gpsMessage() {
        return gpsMessage;
    }

    public long gpsWaitPeriod() {
        return gpsWaitPeriod;
    }

    public long networkWaitPeriod() {
        return networkWaitPeriod;
    }
    // endregion


    public static class Builder {

        private float acceptableAccuracy = ConfigurationDefaults.MIN_ACCURACY;
        private long acceptableTimePeriod = ConfigurationDefaults.TIME_PERIOD;
        private long gpsWaitPeriod = ConfigurationDefaults.WAIT_PERIOD;
        private long networkWaitPeriod = ConfigurationDefaults.WAIT_PERIOD;
        private String gpsMessage = ConfigurationDefaults.EMPTY_STRING;

        /**
         * Minimum Accuracy that you seek location to be
         * <p>
         * Default is {@linkplain Default#MIN_ACCURACY}
         */
        public Builder acceptableAccuracy(float acceptableAccuracy) {
            this.acceptableAccuracy = acceptableAccuracy;
            return this;
        }

        /**
         * Indicates time period that can be count as usable location,
         * this needs to be considered such as "last 5 minutes"
         * <p>
         * Default is {@linkplain Default#TIME_PERIOD}
         */
        public Builder acceptableTimePeriod(long acceptableTimePeriod) {
            if (acceptableTimePeriod < 0)
                throw new IllegalArgumentException("Acceptable time period cannot be set to minus values.");

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
         * Indicates waiting time period before switching to next possible provider.
         * Possible to set provider wait periods separately by passing providerType as one of the
         * {@linkplain ProviderType.Source} values.
         * <p>
         * Default values are {@linkplain Default#WAIT_PERIOD}
         */
        public Builder setWaitPeriod(@ProviderType.Source int providerType, long milliseconds) {
            if (milliseconds < 0)
                throw new IllegalArgumentException("Wait period cannot be set to minus values.");

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
            return new DefaultProviderConfiguration(this);
        }
    }
}
