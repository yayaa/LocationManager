package com.yayandroid.locationmanager.configuration;

import android.Manifest;

import com.yayandroid.locationmanager.fakes.MockDialogProvider;
import com.yayandroid.locationmanager.providers.dialogprovider.SimpleMessageDialogProvider;
import com.yayandroid.locationmanager.providers.permissionprovider.DefaultPermissionProvider;

import org.hamcrest.CoreMatchers;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import java.util.Arrays;

import static org.assertj.core.api.Assertions.assertThat;

public class PermissionConfigurationTest {

    @Rule public ExpectedException expectedException = ExpectedException.none();

    @Test public void checkDefaultValues() {
        PermissionConfiguration configuration = new PermissionConfiguration.Builder().build();
        assertThat(configuration.permissionProvider())
              .isNotNull()
              .isExactlyInstanceOf(DefaultPermissionProvider.class);

        assertThat(configuration.permissionProvider().getDialogProvider()).isNull();
        assertThat(configuration.permissionProvider().getRequiredPermissions())
              .isNotEmpty()
              .isEqualTo(new String[] { Manifest.permission.ACCESS_COARSE_LOCATION,
                    Manifest.permission.ACCESS_FINE_LOCATION });
    }

    @Test public void requiredPermissionsShouldThrowExceptionWhenSetToNull() {
        expectedException.expect(IllegalArgumentException.class);
        expectedException.expectMessage(CoreMatchers.startsWith("requiredPermissions"));

        new PermissionConfiguration.Builder().requiredPermissions(null);
    }

    @Test public void requiredPermissionsShouldThrowExceptionWhenSetEmpty() {
        expectedException.expect(IllegalArgumentException.class);
        expectedException.expectMessage(CoreMatchers.startsWith("requiredPermissions"));

        new PermissionConfiguration.Builder().requiredPermissions(new String[]{});
    }

    @Test public void requiredPermissionsShouldSetPermissionsWhenSetNotEmpty() {
        PermissionConfiguration permissionConfiguration = new PermissionConfiguration.Builder().requiredPermissions(Defaults.LOCATION_PERMISSIONS).build();

        assertThat(permissionConfiguration.permissionProvider().getRequiredPermissions()).containsAll(Arrays.asList(Defaults.LOCATION_PERMISSIONS));
    }

    @Test public void whenRationaleMessageIsNotEmptyDefaultDialogProviderShouldBeSimple() {
        final String RATIONALE_MESSAGE = "some_text";
        PermissionConfiguration configuration = new PermissionConfiguration.Builder()
              .rationaleMessage(RATIONALE_MESSAGE)
              .build();

        assertThat(configuration.permissionProvider().getDialogProvider())
              .isNotNull()
              .isExactlyInstanceOf(SimpleMessageDialogProvider.class);
        assertThat(((SimpleMessageDialogProvider) configuration.permissionProvider().getDialogProvider()).message())
              .isEqualTo(RATIONALE_MESSAGE);
    }

    @Test public void whenDialogProviderIsSetMessageShouldBeIgnored() {
        final String RATIONALE_MESSAGE = "some_text";
        PermissionConfiguration configuration = new PermissionConfiguration.Builder()
              .rationaleDialogProvider(new MockDialogProvider(RATIONALE_MESSAGE))
              .rationaleMessage("ignored_text")
              .build();

        assertThat(configuration.permissionProvider().getDialogProvider())
              .isNotNull()
              .isExactlyInstanceOf(MockDialogProvider.class);
        assertThat(((MockDialogProvider) configuration.permissionProvider().getDialogProvider()).message())
              .isEqualTo(RATIONALE_MESSAGE);
    }

}