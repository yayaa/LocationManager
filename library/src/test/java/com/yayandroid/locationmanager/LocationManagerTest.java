package com.yayandroid.locationmanager;

import android.content.Intent;

import com.yayandroid.locationmanager.LocationManager.Builder;
import com.yayandroid.locationmanager.configuration.LocationConfiguration;
import com.yayandroid.locationmanager.constants.FailType;
import com.yayandroid.locationmanager.constants.ProcessType;
import com.yayandroid.locationmanager.listener.LocationListener;
import com.yayandroid.locationmanager.providers.locationprovider.DispatcherLocationProvider;
import com.yayandroid.locationmanager.providers.locationprovider.LocationProvider;
import com.yayandroid.locationmanager.providers.permissionprovider.PermissionProvider;
import com.yayandroid.locationmanager.view.ContextProcessor;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.mockito.Answers;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class LocationManagerTest {

    @Rule public ExpectedException expectedException = ExpectedException.none();

    @Mock ContextProcessor contextProcessor;
    @Mock LocationListener locationListener;
    @Mock LocationProvider locationProvider;
    @Mock PermissionProvider permissionProvider;

    @Mock(answer = Answers.RETURNS_DEEP_STUBS)
    LocationConfiguration locationConfiguration;

    @Before
    public void setUp() throws Exception {
        MockitoAnnotations.initMocks(this);
        when(locationConfiguration.permissionConfiguration().permissionProvider()).thenReturn(permissionProvider);
    }

    @Test public void buildingWithoutContextProcessorShouldThrowException() {
        expectedException.expect(IllegalStateException.class);

        //noinspection ConstantConditions
        new Builder(((ContextProcessor) null))
                .locationProvider(locationProvider)
                .notify(locationListener)
                .build();
    }

    // region Build Tests
    @Test public void buildingWithoutConfigurationShouldThrowException() {
        expectedException.expect(IllegalStateException.class);

        new LocationManager.Builder(contextProcessor)
              .locationProvider(locationProvider)
              .notify(locationListener)
              .build();
    }

    @Test public void buildingWithoutProviderShouldUseDispatcherLocationProvider() {
        LocationManager locationManager = new Builder(contextProcessor)
              .configuration(locationConfiguration)
              .notify(locationListener)
              .build();

        assertThat(locationManager.activeProvider())
              .isNotNull()
              .isExactlyInstanceOf(DispatcherLocationProvider.class);
    }

    @Test public void buildingShouldCallConfigureAndSetListenerOnProvider() {
        buildLocationManager();

        verify(locationProvider).configure(contextProcessor, locationConfiguration, locationListener);
    }

    @Test public void buildingShouldSetContextProcessorAndListenerToPermissionListener() {
        LocationManager locationManager = buildLocationManager();

        verify(permissionProvider).setContextProcessor(contextProcessor);
        verify(permissionProvider).setPermissionListener(locationManager);
    }
    // endregion

    // region Redirect Tests
    @Test public void whenOnPauseShouldRedirectToLocationProvider() {
        LocationManager locationManager = buildLocationManager();

        locationManager.onPause();

        verify(locationProvider).onPause();
    }

    @Test public void whenOnResumeShouldRedirectToLocationProvider() {
        LocationManager locationManager = buildLocationManager();

        locationManager.onResume();

        verify(locationProvider).onResume();
    }

    @Test public void whenOnDestroyShouldRedirectToLocationProvider() {
        LocationManager locationManager = buildLocationManager();

        locationManager.onDestroy();

        verify(locationProvider).onDestroy();
    }

    @Test public void whenCancelShouldRedirectToLocationProvider() {
        LocationManager locationManager = buildLocationManager();

        locationManager.cancel();

        verify(locationProvider).cancel();
    }

    @Test public void whenOnActivityResultShouldRedirectToLocationProvider() {
        LocationManager locationManager = buildLocationManager();
        int requestCode = 1;
        int resultCode = 2;
        Intent data = new Intent();

        locationManager.onActivityResult(requestCode, resultCode, data);

        verify(locationProvider).onActivityResult(eq(requestCode), eq(resultCode), eq(data));
    }

    @Test public void whenOnRequestPermissionsResultShouldRedirectToPermissionProvider() {
        LocationManager locationManager = buildLocationManager();
        int requestCode = 1;
        String[] permissions = new String[1];
        int[] grantResults = new int[1];

        locationManager.onRequestPermissionsResult(requestCode, permissions, grantResults);

        verify(permissionProvider).onRequestPermissionsResult(eq(requestCode), eq(permissions), eq(grantResults));
    }

    @Test public void whenGetShouldRedirectToLocationProviderWhenPermissionIsGranted() {
        when(permissionProvider.hasPermission()).thenReturn(true);
        LocationManager locationManager = buildLocationManager();

        locationManager.get();

        verify(locationProvider).get();
    }
    // endregion

    // region Retrieve Tests
    @Test public void isWaitingForLocationShouldRetrieveFromLocationProvider() {
        when(locationProvider.isWaiting()).thenReturn(true);
        LocationManager locationManager = buildLocationManager();

        assertThat(locationManager.isWaitingForLocation()).isTrue();
        verify(locationProvider).isWaiting();
    }

    @Test public void isAnyDialogShowingShouldRetrieveFromLocationProvider() {
        when(locationProvider.isDialogShowing()).thenReturn(true);
        LocationManager locationManager = buildLocationManager();

        assertThat(locationManager.isAnyDialogShowing()).isTrue();
        verify(locationProvider).isDialogShowing();
    }
    // endregion

    @Test public void whenGetCalledShouldStartPermissionRequest() {
        LocationManager locationManager = buildLocationManager();

        locationManager.get();

        verify(permissionProvider).hasPermission();
        verify(permissionProvider).requestPermissions();
    }

    @Test public void whenRequestPermissionsAreAlreadyGrantedShouldNotifyListenerWithTrue() {
        when(permissionProvider.hasPermission()).thenReturn(true);
        LocationManager locationManager = buildLocationManager();

        locationManager.askForPermission();

        verify(locationListener).onPermissionGranted(eq(true));
    }

    @Test public void whenRequestedPermissionsAreGrantedShouldNotifyListenerWithFalse() {
        LocationManager locationManager = buildLocationManager();
        when(permissionProvider.getPermissionListener()).thenReturn(locationManager);

        permissionProvider.getPermissionListener().onPermissionsGranted();

        verify(locationListener).onPermissionGranted(eq(false));
    }

    @Test public void whenRequestedPermissionsAreDeniedShouldCallFailOnListener() {
        LocationManager locationManager = buildLocationManager();
        when(permissionProvider.getPermissionListener()).thenReturn(locationManager);

        permissionProvider.getPermissionListener().onPermissionsDenied();

        //noinspection WrongConstant
        verify(locationListener).onLocationFailed(eq(FailType.PERMISSION_DENIED));
    }

    @Test public void whenAskForPermissionShouldNotifyListenerWithProcessTypeChanged() {
        LocationManager locationManager = buildLocationManager();

        locationManager.askForPermission();

        //noinspection WrongConstant
        verify(locationListener).onProcessTypeChanged(eq(ProcessType.ASKING_PERMISSIONS));
    }

    @Test public void whenRequestingPermissionIsNotPossibleThenItShouldFail() {
        when(permissionProvider.requestPermissions()).thenReturn(false);
        LocationManager locationManager = buildLocationManager();

        locationManager.askForPermission();

        //noinspection WrongConstant
        verify(locationListener).onLocationFailed(eq(FailType.PERMISSION_DENIED));
    }

    private LocationManager buildLocationManager() {
        return new Builder(contextProcessor)
              .locationProvider(locationProvider)
              .configuration(locationConfiguration)
              .notify(locationListener)
              .build();
    }

}