package com.yayandroid.locationmanager.configuration;

import com.google.android.gms.location.LocationRequest;
import com.yayandroid.locationmanager.configuration.GooglePlayServicesConfiguration.Builder;

import org.hamcrest.CoreMatchers;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import static org.assertj.core.api.Assertions.assertThat;

public class GooglePlayServicesConfigurationTest {

    private static final int SECOND = 1000;
    private static final int MINUTE = 60 * SECOND;

    @Rule public ExpectedException expectedException = ExpectedException.none();

    @Test public void checkDefaultValues() {
        GooglePlayServicesConfiguration configuration = new GooglePlayServicesConfiguration.Builder().build();
        assertThat(configuration.locationRequest()).isEqualTo(createDefaultLocationRequest());
        assertThat(configuration.fallbackToDefault()).isTrue();
        assertThat(configuration.askForGooglePlayServices()).isFalse();
        assertThat(configuration.askForSettingsApi()).isTrue();
        assertThat(configuration.failOnSettingsApiSuspended()).isFalse();
        assertThat(configuration.ignoreLastKnowLocation()).isFalse();
        assertThat(configuration.googlePlayServicesWaitPeriod()).isEqualTo(20 * SECOND);
    }

    @Test public void setWaitPeriodShouldThrowExceptionWhenGooglePlayServicesWaitPeriodIsSet() {
        expectedException.expect(IllegalArgumentException.class);
        expectedException.expectMessage(CoreMatchers.startsWith("waitPeriod"));

        new GooglePlayServicesConfiguration.Builder().setWaitPeriod(-1);
    }

    @Test public void clonesShouldShareSameInstances() {
        GooglePlayServicesConfiguration configuration = new Builder().build();

        GooglePlayServicesConfiguration firstClone = configuration.newBuilder().build();
        GooglePlayServicesConfiguration secondClone = configuration.newBuilder().build();

        assertThat(firstClone.locationRequest())
              .isEqualTo(secondClone.locationRequest())
              .isEqualTo(createDefaultLocationRequest());
        assertThat(firstClone.askForGooglePlayServices())
              .isEqualTo(secondClone.askForGooglePlayServices())
              .isFalse();
        assertThat(firstClone.askForSettingsApi())
              .isEqualTo(secondClone.askForSettingsApi())
              .isTrue();
        assertThat(firstClone.failOnSettingsApiSuspended())
              .isEqualTo(secondClone.failOnSettingsApiSuspended())
              .isFalse();
        assertThat(firstClone.ignoreLastKnowLocation())
              .isEqualTo(secondClone.ignoreLastKnowLocation())
              .isFalse();
        assertThat(firstClone.googlePlayServicesWaitPeriod())
              .isEqualTo(secondClone.googlePlayServicesWaitPeriod())
              .isEqualTo(20 * SECOND);
    }

    private LocationRequest createDefaultLocationRequest() {
        return LocationRequest.create()
              .setPriority(LocationRequest.PRIORITY_BALANCED_POWER_ACCURACY)
              .setInterval(5 * MINUTE)
              .setFastestInterval(MINUTE);
    }

}
