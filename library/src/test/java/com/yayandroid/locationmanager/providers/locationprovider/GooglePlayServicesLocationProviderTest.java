package com.yayandroid.locationmanager.providers.locationprovider;

import android.app.Activity;
import android.content.Context;
import android.content.IntentSender.SendIntentException;
import android.location.Location;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.LocationSettingsResult;
import com.google.android.gms.location.LocationSettingsStatusCodes;
import com.yayandroid.locationmanager.configuration.GooglePlayServicesConfiguration;
import com.yayandroid.locationmanager.configuration.LocationConfiguration;
import com.yayandroid.locationmanager.constants.FailType;
import com.yayandroid.locationmanager.constants.ProcessType;
import com.yayandroid.locationmanager.constants.RequestCode;
import com.yayandroid.locationmanager.listener.FallbackListener;
import com.yayandroid.locationmanager.listener.LocationListener;
import com.yayandroid.locationmanager.view.ContextProcessor;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

public class GooglePlayServicesLocationProviderTest {

    @Mock GooglePlayServicesLocationSource mockedSource;

    @Mock Location location;
    @Mock Context context;
    @Mock Activity activity;

    @Mock ContextProcessor contextProcessor;
    @Mock LocationListener locationListener;

    @Mock LocationConfiguration locationConfiguration;
    @Mock GooglePlayServicesConfiguration googlePlayServicesConfiguration;
    @Mock FallbackListener fallbackListener;

    private GooglePlayServicesLocationProvider googlePlayServicesLocationProvider;

    @Before
    public void setUp() throws Exception {
        MockitoAnnotations.initMocks(this);

        googlePlayServicesLocationProvider = spy(new GooglePlayServicesLocationProvider(fallbackListener));
        googlePlayServicesLocationProvider.configure(contextProcessor, locationConfiguration, locationListener);
        googlePlayServicesLocationProvider.setDispatcherLocationSource(mockedSource);

        when(locationConfiguration.googlePlayServicesConfiguration()).thenReturn(googlePlayServicesConfiguration);
        when(contextProcessor.getContext()).thenReturn(context);
        when(contextProcessor.getActivity()).thenReturn(activity);
    }

    @Test
    public void onResumeShouldNotConnectGoogleApiClientWhenSettingsDialogIsOnTrue() {
        makeSettingsDialogIsOnTrue();

        googlePlayServicesLocationProvider.onResume();

        verify(mockedSource, never()).connectGoogleApiClient();
    }

    @Test
    public void onResumeShouldNotConnectWhenLocationIsAlreadyProvidedAndNotRequiredToKeepTracking() {
        when(locationConfiguration.keepTracking()).thenReturn(false);

        googlePlayServicesLocationProvider.onResume();

        verify(mockedSource, never()).clearGoogleApiClient();
    }

    @Test
    public void onResumeShouldConnectWhenLocationIsNotYetProvided() {
        googlePlayServicesLocationProvider.setWaiting(true);

        googlePlayServicesLocationProvider.onResume();

        verify(mockedSource).connectGoogleApiClient();
    }

    @Test
    public void onResumeShouldConnectWhenLocationIsAlreadyProvidedButRequiredToKeepTracking() {
        googlePlayServicesLocationProvider.setWaiting(true);
        when(locationConfiguration.keepTracking()).thenReturn(true);

        googlePlayServicesLocationProvider.onResume();

        verify(mockedSource).connectGoogleApiClient();
    }

    @Test
    public void onPauseShouldNotDisconnectGoogleApiClientWhenSettingsDialogIsOnTrue() {
        makeSettingsDialogIsOnTrue();

        googlePlayServicesLocationProvider.onPause();

        verify(mockedSource, never()).disconnectGoogleApiClient();
    }

    @Test
    public void onPauseShouldNotDisconnectGoogleApiClientWhenItsNotConnected() {
        when(mockedSource.isGoogleApiClientConnected()).thenReturn(false);

        googlePlayServicesLocationProvider.onPause();

        verify(mockedSource, never()).disconnectGoogleApiClient();
    }

    @Test
    public void onPauseShouldDisconnectGoogleApiClientWhenItIsConnected() {
        when(mockedSource.isGoogleApiClientConnected()).thenReturn(true);

        googlePlayServicesLocationProvider.onPause();

        verify(mockedSource).disconnectGoogleApiClient();
    }

    @Test
    public void onDestroyShouldClearGoogleApiClient() {
        googlePlayServicesLocationProvider.onDestroy();

        verify(mockedSource).clearGoogleApiClient();
    }

    @Test
    public void isDialogShownShouldReturnFalseWhenSettingsApiDialogIsNotShown() {
        assertThat(googlePlayServicesLocationProvider.isDialogShowing()).isFalse();
    }

    @Test
    public void isDialogShownShouldReturnTrueWhenSettingsApiDialogShown() {
        makeSettingsDialogIsOnTrue();

        assertThat(googlePlayServicesLocationProvider.isDialogShowing()).isTrue();
    }

    @Test
    public void getShouldSetWaitingTrue() {
        assertThat(googlePlayServicesLocationProvider.isWaiting()).isFalse();

        googlePlayServicesLocationProvider.get();

        assertThat(googlePlayServicesLocationProvider.isWaiting()).isTrue();
    }

    @Test
    public void getShouldFailWhenThereIsNoContext() {
        when(contextProcessor.getContext()).thenReturn(null);

        googlePlayServicesLocationProvider.get();

        verify(locationListener).onLocationFailed(FailType.VIEW_DETACHED);
    }

    @Test
    public void getShouldConnectGoogleApiClient() {
        googlePlayServicesLocationProvider.get();

        verify(mockedSource).connectGoogleApiClient();
    }

    @Test
    public void cancelShouldRemoveLocationRequestAndDisconnectWhenGoogleApiClientIsConnected() {
        when(mockedSource.isGoogleApiClientConnected()).thenReturn(true);

        googlePlayServicesLocationProvider.cancel();

        verify(mockedSource).removeLocationUpdates();
        verify(mockedSource).disconnectGoogleApiClient();
    }

    @Test
    public void onActivityResultShouldSetDialogShownToFalse() {
        makeSettingsDialogIsOnTrue();
        assertThat(googlePlayServicesLocationProvider.isDialogShowing()).isTrue();

        googlePlayServicesLocationProvider.onActivityResult(RequestCode.SETTINGS_API, -1, null);

        assertThat(googlePlayServicesLocationProvider.isDialogShowing()).isFalse();
    }

    @Test
    public void onActivityResultShouldRequestLocationUpdateWhenResultIsOk() {
        googlePlayServicesLocationProvider.onActivityResult(RequestCode.SETTINGS_API, Activity.RESULT_OK, null);

        verify(googlePlayServicesLocationProvider).requestLocationUpdate();
    }

    @Test
    public void onActivityResultShouldCallSettingsApiFailWhenResultIsNotOk() {
        googlePlayServicesLocationProvider.onActivityResult(RequestCode.SETTINGS_API, Activity.RESULT_CANCELED, null);

        verify(googlePlayServicesLocationProvider).settingsApiFail(FailType.GOOGLE_PLAY_SERVICES_SETTINGS_DENIED);
    }

    @Test
    public void onConnectedShouldNotCheckLastKnowLocationWhenRequirementsIgnore() {
        when(googlePlayServicesConfiguration.ignoreLastKnowLocation()).thenReturn(true);

        googlePlayServicesLocationProvider.onConnected(null);

        verify(googlePlayServicesLocationProvider, never()).checkLastKnowLocation();
    }

    @Test
    public void onConnectedShouldCheckLastKnowLocation() {
        googlePlayServicesLocationProvider.onConnected(null);

        verify(googlePlayServicesLocationProvider).checkLastKnowLocation();
    }

    @Test
    public void onConnectedShouldNotCallLocationRequiredWhenLastKnowIsReadyAndNoNeedToKeepTracking() {
        when(mockedSource.getLocationAvailability()).thenReturn(true);
        when(mockedSource.getLastLocation()).thenReturn(new Location(""));
        when(locationConfiguration.keepTracking()).thenReturn(false);

        googlePlayServicesLocationProvider.waitingForConnectionToRequestLocationUpdate(false);
        googlePlayServicesLocationProvider.onConnected(null);

        verify(googlePlayServicesLocationProvider, never()).locationRequired();
    }

    @Test
    public void onConnectedShouldSwitchWaitingForConnectionToRequestLocationUpdateOff() {
        // Have first condition false
        when(locationConfiguration.keepTracking()).thenReturn(false);

        // Have second condition false
        when(googlePlayServicesConfiguration.ignoreLastKnowLocation()).thenReturn(false);
        when(mockedSource.getLocationAvailability()).thenReturn(true);
        when(mockedSource.getLastLocation()).thenReturn(location);

        // waitingForConnectionToRequestLocationUpdate is true by default

        googlePlayServicesLocationProvider.onConnected(null);

        verify(googlePlayServicesLocationProvider).waitingForConnectionToRequestLocationUpdate(false);
        verify(googlePlayServicesLocationProvider).locationRequired();
    }

    @Test
    public void onConnectedShouldNotCallLocationRequiredWhenLastKnowIsNotAvailable() {
        when(mockedSource.getLocationAvailability()).thenReturn(false);

        googlePlayServicesLocationProvider.onConnected(null);

        verify(googlePlayServicesLocationProvider).locationRequired();
    }

    @Test
    public void onConnectedShouldCallLocationRequiredWhenConfigurationRequiresKeepTracking() {
        when(mockedSource.getLocationAvailability()).thenReturn(true);
        when(mockedSource.getLastLocation()).thenReturn(location);
        when(locationConfiguration.keepTracking()).thenReturn(true);

        googlePlayServicesLocationProvider.onConnected(null);

        verify(googlePlayServicesLocationProvider).locationRequired();
    }

    @Test
    public void onConnectionSuspendedShouldFailWhenConfigurationRequires() {
        when(googlePlayServicesConfiguration.failOnConnectionSuspended()).thenReturn(true);

        googlePlayServicesLocationProvider.onConnectionSuspended(1);

        verify(locationListener).onLocationFailed(FailType.GOOGLE_PLAY_SERVICES_CONNECTION_FAIL);
    }

    @Test
    public void onConnectionSuspendedShouldRetry() {
        when(googlePlayServicesConfiguration.suspendedConnectionRetryCount()).thenReturn(1);

        googlePlayServicesLocationProvider.onConnectionSuspended(1);

        verify(mockedSource).connectGoogleApiClient();
    }

    @Test
    public void onConnectionSuspendedShouldFailWhenRequiredIterationsCompleted() {
        when(googlePlayServicesConfiguration.suspendedConnectionRetryCount()).thenReturn(1);

        googlePlayServicesLocationProvider.onConnectionSuspended(1);
        verify(mockedSource).connectGoogleApiClient();
        googlePlayServicesLocationProvider.onConnectionSuspended(1);

        verifyNoMoreInteractions(mockedSource);
        verify(locationListener).onLocationFailed(FailType.GOOGLE_PLAY_SERVICES_CONNECTION_FAIL);
    }

    @Test
    public void onConnectionFailedShouldCallFailOnListener() {
        googlePlayServicesLocationProvider.onConnectionFailed(new ConnectionResult(1));

        verify(locationListener).onLocationFailed(FailType.GOOGLE_PLAY_SERVICES_CONNECTION_FAIL);
    }

    @Test
    public void onLocationChangedShouldNotifyListener() {
        googlePlayServicesLocationProvider.onLocationChanged(location);

        verify(locationListener).onLocationChanged(location);
    }

    @Test
    public void onLocationChangedShouldSetWaitingFalse() {
        googlePlayServicesLocationProvider.setWaiting(true);
        assertThat(googlePlayServicesLocationProvider.isWaiting()).isTrue();

        googlePlayServicesLocationProvider.onLocationChanged(location);

        assertThat(googlePlayServicesLocationProvider.isWaiting()).isFalse();
    }

    @Test
    public void onLocationChangedShouldRemoveUpdateLocationWhenKeepTrackingIsNotRequired() {
        when(locationConfiguration.keepTracking()).thenReturn(false);
        when(mockedSource.isGoogleApiClientConnected()).thenReturn(true);

        googlePlayServicesLocationProvider.onLocationChanged(location);

        verify(mockedSource).removeLocationUpdates();
    }

    @Test
    public void onLocationChangedShouldNotRemoveUpdateLocationWhenGoogleApiClientIsNotConnected() {
        when(locationConfiguration.keepTracking()).thenReturn(false);
        when(mockedSource.isGoogleApiClientConnected()).thenReturn(false);

        googlePlayServicesLocationProvider.onLocationChanged(location);

        verify(mockedSource, never()).removeLocationUpdates();
    }

    @Test
    public void onLocationChangedShouldNotRemoveUpdateLocationWhenKeepTrackingIsRequired() {
        when(locationConfiguration.keepTracking()).thenReturn(true);

        googlePlayServicesLocationProvider.onLocationChanged(location);

        verify(mockedSource, never()).removeLocationUpdates();
    }

    @Test
    public void onResultShouldCallRequestLocationUpdateWhenSuccess() {
        googlePlayServicesLocationProvider.onResult(getSettingsResultWith(LocationSettingsStatusCodes.SUCCESS));

        verify(googlePlayServicesLocationProvider).requestLocationUpdate();
    }

    @Test
    public void onResultShouldCallSettingsApiFailWhenChangeUnavailable() {
        googlePlayServicesLocationProvider
              .onResult(getSettingsResultWith(LocationSettingsStatusCodes.SETTINGS_CHANGE_UNAVAILABLE));

        verify(googlePlayServicesLocationProvider).settingsApiFail(FailType.GOOGLE_PLAY_SERVICES_SETTINGS_DIALOG);
    }

    @Test
    public void onResultShouldCallResolveSettingsApiWhenResolutionRequired() {
        LocationSettingsResult settingsResultWith = getSettingsResultWith(LocationSettingsStatusCodes.RESOLUTION_REQUIRED);
        googlePlayServicesLocationProvider.onResult(settingsResultWith);

        verify(googlePlayServicesLocationProvider).resolveSettingsApi(settingsResultWith.getStatus());
    }

    @Test
    public void resolveSettingsApiShouldCallSettingsApiFailWhenThereIsNoActivity() {
        when(contextProcessor.getActivity()).thenReturn(null);

        googlePlayServicesLocationProvider.resolveSettingsApi(new Status(1));

        verify(googlePlayServicesLocationProvider).settingsApiFail(FailType.VIEW_NOT_REQUIRED_TYPE);
    }

    @Test
    public void resolveSettingsApiShouldStartSettingsApiResolutionForResult() throws Exception {
        Status status = new Status(1);
        googlePlayServicesLocationProvider.resolveSettingsApi(status);

        verify(mockedSource).startSettingsApiResolutionForResult(status, activity);
    }

    @Test
    public void resolveSettingsApiShouldCallSettingsApiFailWhenExceptionThrown() throws Exception {
        Status status = new Status(1);
        doThrow(new SendIntentException()).when(mockedSource).startSettingsApiResolutionForResult(status, activity);

        googlePlayServicesLocationProvider.resolveSettingsApi(status);

        verify(googlePlayServicesLocationProvider).settingsApiFail(FailType.GOOGLE_PLAY_SERVICES_SETTINGS_DIALOG);
    }

    @Test
    public void checkLastKnowLocationShouldReturnFalseWhenLocationIsNotAvailable() {
        when(mockedSource.getLocationAvailability()).thenReturn(false);

        assertThat(googlePlayServicesLocationProvider.checkLastKnowLocation()).isFalse();
    }

    @Test
    public void checkLastKnowLocationShouldReturnFalseWhenLocationIsNull() {
        when(mockedSource.getLocationAvailability()).thenReturn(true);

        assertThat(googlePlayServicesLocationProvider.checkLastKnowLocation()).isFalse();
    }

    @Test
    public void checkLastKnowLocationShouldReturnTrueWhenLocationIsAvailable() {
        when(mockedSource.getLocationAvailability()).thenReturn(true);
        when(mockedSource.getLastLocation()).thenReturn(location);

        assertThat(googlePlayServicesLocationProvider.checkLastKnowLocation()).isTrue();
    }

    @Test
    public void locationRequiredShouldCheckLocationSettingsWhenConfigurationAsksForSettingsApi() {
        when(googlePlayServicesConfiguration.askForSettingsApi()).thenReturn(true);

        googlePlayServicesLocationProvider.locationRequired();

        verify(mockedSource).checkLocationSettings();
    }

    @Test
    public void locationRequiredShouldRequestLocationUpdateWhenConfigurationDoesntRequireToAskForSettingsApi() {
        googlePlayServicesLocationProvider.locationRequired();

        verify(googlePlayServicesLocationProvider).requestLocationUpdate();
    }

    @Test
    public void requestLocationUpdateShouldUpdateProcessTypeOnListener() {
        googlePlayServicesLocationProvider.requestLocationUpdate();

        verify(locationListener).onProcessTypeChanged(ProcessType.GETTING_LOCATION_FROM_GOOGLE_PLAY_SERVICES);
    }

    @Test
    public void requestLocationUpdateShouldRequestIfGoogleApiClientIsConnected() {
        when(mockedSource.isGoogleApiClientConnected()).thenReturn(true);

        googlePlayServicesLocationProvider.requestLocationUpdate();

        verify(mockedSource).requestLocationUpdate();
    }

    @Test
    public void requestLocationUpdateShouldTryToConnectIfGoogleApiClientIsNotConnected() {
        when(mockedSource.isGoogleApiClientConnected()).thenReturn(false);

        googlePlayServicesLocationProvider.requestLocationUpdate();

        verify(googlePlayServicesLocationProvider).waitingForConnectionToRequestLocationUpdate(true);
        verify(mockedSource).connectGoogleApiClient();
    }

    @Test
    public void settingsApiFailShouldCallFailWhenConfigurationFailOnSettingsApiSuspendedTrue() {
        when(googlePlayServicesConfiguration.failOnSettingsApiSuspended()).thenReturn(true);

        googlePlayServicesLocationProvider.settingsApiFail(FailType.UNKNOWN);

        verify(googlePlayServicesLocationProvider).failed(FailType.UNKNOWN);
    }

    @Test
    public void settingsApiFailShouldCallFailWhenGoogleApiClientIsNotConnected() {
        when(mockedSource.isGoogleApiClientConnected()).thenReturn(false);

        googlePlayServicesLocationProvider.settingsApiFail(FailType.UNKNOWN);

        verify(googlePlayServicesLocationProvider).failed(FailType.UNKNOWN);
    }

    @Test
    public void settingsApiFailShouldCallRequestLocationUpdateWhenGoogleApiClientIsConnected() {
        when(mockedSource.isGoogleApiClientConnected()).thenReturn(true);

        googlePlayServicesLocationProvider.settingsApiFail(FailType.UNKNOWN);

        verify(googlePlayServicesLocationProvider).requestLocationUpdate();
    }

    @Test
    public void failedShouldRedirectToListenerWhenFallbackToDefaultIsFalse() {
        when(googlePlayServicesConfiguration.fallbackToDefault()).thenReturn(false);

        googlePlayServicesLocationProvider.failed(FailType.UNKNOWN);

        verify(locationListener).onLocationFailed(FailType.UNKNOWN);
    }

    @Test
    public void failedShouldCallFallbackWhenFallbackToDefaultIsTrue() {
        when(googlePlayServicesConfiguration.fallbackToDefault()).thenReturn(true);

        googlePlayServicesLocationProvider.failed(FailType.UNKNOWN);

        verify(fallbackListener).onFallback();
    }

    @Test
    public void failedShouldSetWaitingFalse() {
        googlePlayServicesLocationProvider.setWaiting(true);
        assertThat(googlePlayServicesLocationProvider.isWaiting()).isTrue();

        googlePlayServicesLocationProvider.failed(FailType.UNKNOWN);

        assertThat(googlePlayServicesLocationProvider.isWaiting()).isFalse();
    }

    private void makeSettingsDialogIsOnTrue() {
        when(mockedSource.isGoogleApiClientConnected()).thenReturn(false);
        googlePlayServicesLocationProvider.onResult(getSettingsResultWith(LocationSettingsStatusCodes.RESOLUTION_REQUIRED));
    }

    private LocationSettingsResult getSettingsResultWith(int statusCode) {
        return new LocationSettingsResult(new Status(statusCode, null, null));
    }
}