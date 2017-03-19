package com.yayandroid.locationmanager.configuration;

import com.yayandroid.locationmanager.providers.dialogprovider.SimpleMessageDialogProvider;
import com.yayandroid.locationmanager.providers.permissionprovider.DefaultPermissionProvider;
import com.yayandroid.locationmanager.providers.permissionprovider.StubPermissionProvider;

import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class ConfigurationsTest {

    @Test public void silentConfigurationWithoutParameterShouldKeepTracking() {
        assertThat(Configurations.silentConfiguration().keepTracking()).isTrue();
    }

    @Test public void silentConfigurationCheckDefaultValues() {
        LocationConfiguration silentConfiguration = Configurations.silentConfiguration(false);

        assertThat(silentConfiguration.keepTracking()).isFalse();
        assertThat(silentConfiguration.permissionConfiguration()).isNotNull();
        assertThat(silentConfiguration.permissionConfiguration().permissionProvider())
              .isNotNull()
              .isExactlyInstanceOf(StubPermissionProvider.class);
        assertThat(silentConfiguration.googlePlayServicesConfiguration()).isNotNull();
        assertThat(silentConfiguration.googlePlayServicesConfiguration().askForSettingsApi()).isFalse();
        assertThat(silentConfiguration.defaultProviderConfiguration()).isNotNull();
    }

    @Test public void defaultConfigurationCheckDefaultValues() {
        LocationConfiguration defaultConfiguration = Configurations.defaultConfiguration("rationale", "gps");

        assertThat(defaultConfiguration.keepTracking()).isFalse();
        assertThat(defaultConfiguration.permissionConfiguration()).isNotNull();
        assertThat(defaultConfiguration.permissionConfiguration().permissionProvider())
              .isNotNull()
              .isExactlyInstanceOf(DefaultPermissionProvider.class);
        assertThat(defaultConfiguration.permissionConfiguration().permissionProvider().getDialogProvider())
              .isNotNull()
              .isExactlyInstanceOf(SimpleMessageDialogProvider.class);
        assertThat(((SimpleMessageDialogProvider) defaultConfiguration.permissionConfiguration()
                    .permissionProvider().getDialogProvider()).message()).isEqualTo("rationale");
        assertThat(defaultConfiguration.googlePlayServicesConfiguration()).isNotNull();
        assertThat(defaultConfiguration.defaultProviderConfiguration()).isNotNull();
        assertThat(defaultConfiguration.defaultProviderConfiguration().askForEnableGPS()).isTrue();
        assertThat(defaultConfiguration.defaultProviderConfiguration().gpsDialogProvider())
              .isNotNull()
              .isExactlyInstanceOf(SimpleMessageDialogProvider.class);
        assertThat(((SimpleMessageDialogProvider) defaultConfiguration.defaultProviderConfiguration()
              .gpsDialogProvider()).message()).isEqualTo("gps");

    }
}