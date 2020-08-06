package com.yayandroid.locationmanager.configuration;

import com.yayandroid.locationmanager.constants.ProviderType;
import com.yayandroid.locationmanager.providers.dialogprovider.SimpleMessageDialogProvider;
import com.yayandroid.locationmanager.fakes.MockDialogProvider;

import org.hamcrest.CoreMatchers;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import static org.assertj.core.api.Assertions.assertThat;

public class DefaultProviderConfigurationTest {

    private static final int SECOND = 1000;
    private static final int MINUTE = 60 * SECOND;

    @Rule public ExpectedException expectedException = ExpectedException.none();

    @Test public void checkDefaultValues() {
        DefaultProviderConfiguration configuration = new DefaultProviderConfiguration.Builder().build();

        assertThat(configuration.requiredTimeInterval()).isEqualTo(5 * MINUTE);
        assertThat(configuration.requiredDistanceInterval()).isEqualTo(0);
        assertThat(configuration.acceptableAccuracy()).isEqualTo(5.0f);
        assertThat(configuration.acceptableTimePeriod()).isEqualTo(5 * MINUTE);
        assertThat(configuration.gpsWaitPeriod()).isEqualTo(20 * SECOND);
        assertThat(configuration.networkWaitPeriod()).isEqualTo(20 * SECOND);
        assertThat(configuration.gpsDialogProvider()).isNull();
    }

    @Test public void requiredTimeIntervalShouldThrowExceptionWhenNegative() {
        expectedException.expect(IllegalArgumentException.class);
        expectedException.expectMessage(CoreMatchers.startsWith("requiredTimeInterval"));

        new DefaultProviderConfiguration.Builder().requiredTimeInterval(-1);
    }

    @Test public void requiredDistanceIntervalShouldThrowExceptionWhenNegative() {
        expectedException.expect(IllegalArgumentException.class);
        expectedException.expectMessage(CoreMatchers.startsWith("requiredDistanceInterval"));

        new DefaultProviderConfiguration.Builder().requiredDistanceInterval(-1);
    }

    @Test public void acceptableAccuracyShouldThrowExceptionWhenNegative() {
        expectedException.expect(IllegalArgumentException.class);
        expectedException.expectMessage(CoreMatchers.startsWith("acceptableAccuracy"));

        new DefaultProviderConfiguration.Builder().acceptableAccuracy(-1);
    }

    @Test public void acceptableTimePeriodShouldThrowExceptionWhenNegative() {
        expectedException.expect(IllegalArgumentException.class);
        expectedException.expectMessage(CoreMatchers.startsWith("acceptableTimePeriod"));

        new DefaultProviderConfiguration.Builder().acceptableTimePeriod(-1);
    }

    @Test public void setWaitPeriodShouldThrowExceptionWhenNetworkWaitPeriodIsNegative() {
        expectedException.expect(IllegalArgumentException.class);
        expectedException.expectMessage(CoreMatchers.startsWith("waitPeriod"));

        new DefaultProviderConfiguration.Builder().setWaitPeriod(ProviderType.NETWORK, -1);
    }

    @Test public void setWaitPeriodShouldThrowExceptionWhenGPSWaitPeriodIsNegative() {
        expectedException.expect(IllegalArgumentException.class);
        expectedException.expectMessage(CoreMatchers.startsWith("waitPeriod"));

        new DefaultProviderConfiguration.Builder().setWaitPeriod(ProviderType.GPS, -1);
    }

    @Test public void setWaitPeriodShouldThrowExceptionWhenDefaultProvidersWaitPeriodIsNegative() {
        expectedException.expect(IllegalArgumentException.class);
        expectedException.expectMessage(CoreMatchers.startsWith("waitPeriod"));

        new DefaultProviderConfiguration.Builder().setWaitPeriod(ProviderType.DEFAULT_PROVIDERS, -1);
    }

    @Test public void setWaitPeriodShouldThrowExceptionWhenGooglePlayServicesWaitPeriodIsSet() {
        expectedException.expect(IllegalStateException.class);
        expectedException.expectMessage(CoreMatchers.startsWith("GooglePlayServices"));

        new DefaultProviderConfiguration.Builder().setWaitPeriod(ProviderType.GOOGLE_PLAY_SERVICES, 1);
    }

    @Test public void setWaitPeriodShouldSetPeriodsWhenDefaultProvidersIsSet() {
        DefaultProviderConfiguration providerConfiguration = new DefaultProviderConfiguration.Builder().setWaitPeriod(ProviderType.DEFAULT_PROVIDERS, 1).build();

        assertThat(providerConfiguration.gpsWaitPeriod()).isEqualTo(1);
        assertThat(providerConfiguration.networkWaitPeriod()).isEqualTo(1);
    }

    @Test public void whenGpsMessageAndDialogProviderNotSetAskForGPSEnableShouldReturnFalse() {
        DefaultProviderConfiguration configuration = new DefaultProviderConfiguration.Builder().build();
        assertThat(configuration.askForEnableGPS()).isFalse();
    }

    @Test public void whenGpsMessageSetAskForGPSEnableShouldReturnTrue() {
        DefaultProviderConfiguration configuration = new DefaultProviderConfiguration.Builder()
              .gpsMessage("some_text")
              .build();
        assertThat(configuration.askForEnableGPS()).isTrue();
    }

    @Test public void whenDialogProviderSetAskForGPSEnableShouldReturnTrue() {
        DefaultProviderConfiguration configuration = new DefaultProviderConfiguration.Builder()
              .gpsDialogProvider(new MockDialogProvider("some_text"))
              .build();
        assertThat(configuration.askForEnableGPS()).isTrue();
    }

    @Test public void whenGpsMessageIsEmptyAndDialogProviderIsNotSetThenDialogProviderShouldBeNull() {
        DefaultProviderConfiguration configuration = new DefaultProviderConfiguration.Builder().build();
        assertThat(configuration.gpsDialogProvider()).isNull();
    }

    @Test public void whenGpsMessageIsNotEmptyDefaultDialogProviderShouldBeSimple() {
        final String GPS_MESSAGE = "some_text";
        DefaultProviderConfiguration configuration = new DefaultProviderConfiguration.Builder()
              .gpsMessage(GPS_MESSAGE)
              .build();

        assertThat(configuration.gpsDialogProvider())
              .isNotNull()
              .isExactlyInstanceOf(SimpleMessageDialogProvider.class);
        assertThat(((SimpleMessageDialogProvider) configuration.gpsDialogProvider()).message()).isEqualTo(GPS_MESSAGE);
    }

    @Test public void whenDialogProviderIsSetMessageShouldBeIgnored() {
        final String GPS_MESSAGE = "some_text";
        DefaultProviderConfiguration configuration = new DefaultProviderConfiguration.Builder()
              .gpsMessage("ignored_message")
              .gpsDialogProvider(new MockDialogProvider(GPS_MESSAGE))
              .build();

        assertThat(configuration.gpsDialogProvider())
              .isNotNull()
              .isExactlyInstanceOf(MockDialogProvider.class);
        assertThat(((MockDialogProvider) configuration.gpsDialogProvider()).message()).isEqualTo(GPS_MESSAGE);
    }

    @Test public void clonesShouldShareSameInstances() {
        DefaultProviderConfiguration configuration = new DefaultProviderConfiguration.Builder()
              .gpsDialogProvider(new MockDialogProvider("some_text"))
              .build();

        DefaultProviderConfiguration firstClone = configuration.newBuilder().build();
        DefaultProviderConfiguration secondClone = configuration.newBuilder().build();

        assertThat(firstClone.requiredTimeInterval())
              .isEqualTo(secondClone.requiredTimeInterval())
              .isEqualTo(5 * MINUTE);
        assertThat(firstClone.requiredDistanceInterval())
              .isEqualTo(secondClone.requiredDistanceInterval())
              .isEqualTo(0);
        assertThat(firstClone.acceptableAccuracy())
              .isEqualTo(secondClone.acceptableAccuracy())
              .isEqualTo(5.0f);
        assertThat(firstClone.acceptableTimePeriod())
              .isEqualTo(secondClone.acceptableTimePeriod())
              .isEqualTo(5 * MINUTE);
        assertThat(firstClone.gpsWaitPeriod())
              .isEqualTo(secondClone.gpsWaitPeriod())
              .isEqualTo(20 * SECOND);
        assertThat(firstClone.networkWaitPeriod())
              .isEqualTo(secondClone.networkWaitPeriod())
              .isEqualTo(20 * SECOND);
        assertThat(firstClone.gpsDialogProvider())
              .isEqualTo(secondClone.gpsDialogProvider())
              .isNotNull()
              .isExactlyInstanceOf(MockDialogProvider.class);
    }

}