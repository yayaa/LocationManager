package com.yayandroid.locationmanager.configuration;

import com.yayandroid.locationmanager.providers.permissionprovider.DefaultPermissionProvider;
import com.yayandroid.locationmanager.providers.permissionprovider.StubPermissionProvider;

import org.hamcrest.CoreMatchers;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import static org.assertj.core.api.Assertions.assertThat;

public class LocationConfigurationTest {

    @Rule public ExpectedException expectedException = ExpectedException.none();

    @Test public void whenNoProviderConfigurationIsSetBuildShouldThrowException() {
        expectedException.expect(IllegalStateException.class);
        expectedException.expectMessage(
              CoreMatchers.containsString("GooglePlayServicesConfiguration and DefaultProviderConfiguration"));

        new LocationConfiguration.Builder().build();
    }

    @Test public void checkDefaultValues() {
        LocationConfiguration configuration = getConfiguration();
        assertThat(configuration.keepTracking()).isFalse();
    }

    @Test public void whenNoPermissionConfigurationIsSetDefaultConfigurationShouldContainStubProvider() {
        LocationConfiguration configuration = getConfiguration();

        assertThat(configuration.permissionConfiguration()).isNotNull();
        assertThat(configuration.permissionConfiguration().permissionProvider())
              .isNotNull()
              .isExactlyInstanceOf(StubPermissionProvider.class);
    }

    @Test public void clonesShouldShareSameInstances() {
        LocationConfiguration configuration = getConfiguration();

        LocationConfiguration firstClone = configuration.newBuilder().build();
        LocationConfiguration secondClone = configuration.newBuilder().build();

        assertThat(firstClone.keepTracking())
              .isEqualTo(secondClone.keepTracking())
              .isFalse();
        assertThat(firstClone.permissionConfiguration())
              .isEqualTo(secondClone.permissionConfiguration())
              .isNotNull();
        assertThat(firstClone.defaultProviderConfiguration())
              .isEqualTo(secondClone.defaultProviderConfiguration())
              .isNotNull();
        assertThat(firstClone.googlePlayServicesConfiguration())
              .isEqualTo(secondClone.googlePlayServicesConfiguration())
              .isNotNull();
    }

    @Test public void clonedConfigurationIsIndependent() {
        LocationConfiguration configuration = getConfiguration();
        LocationConfiguration clone = configuration.newBuilder()
              .askForPermission(new PermissionConfiguration.Builder().build())
              .build();

        assertThat(configuration.permissionConfiguration())
              .isNotEqualTo(clone.permissionConfiguration());
        assertThat(configuration.permissionConfiguration().permissionProvider())
              .isNotNull()
              .isExactlyInstanceOf(StubPermissionProvider.class);
        assertThat(clone.permissionConfiguration().permissionProvider())
              .isNotNull()
              .isExactlyInstanceOf(DefaultPermissionProvider.class);
    }

    private LocationConfiguration getConfiguration() {
        return new LocationConfiguration.Builder()
              .useDefaultProviders(new DefaultProviderConfiguration.Builder().build())
              .useGooglePlayServices(new GooglePlayServicesConfiguration.Builder().build())
              .build();
    }

}
