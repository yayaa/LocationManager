package com.yayandroid.locationmanager.providers.locationprovider;

import android.app.Activity;
import android.content.Context;
import android.content.IntentSender.SendIntentException;
import android.location.Location;

import androidx.annotation.NonNull;

import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.common.api.ResolvableApiException;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationSettingsResponse;
import com.google.android.gms.location.LocationSettingsResult;
import com.google.android.gms.location.LocationSettingsStatusCodes;
import com.yayandroid.locationmanager.configuration.GooglePlayServicesConfiguration;
import com.yayandroid.locationmanager.configuration.LocationConfiguration;
import com.yayandroid.locationmanager.constants.FailType;
import com.yayandroid.locationmanager.constants.ProcessType;
import com.yayandroid.locationmanager.constants.RequestCode;
import com.yayandroid.locationmanager.listener.FallbackListener;
import com.yayandroid.locationmanager.listener.LocationListener;
import com.yayandroid.locationmanager.fakes.FakeSimpleTask;
import com.yayandroid.locationmanager.view.ContextProcessor;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class GooglePlayServicesLocationProviderTest {

    @Mock
    GooglePlayServicesLocationSource mockedSource;

    @Mock
    Location location;
    @Mock
    Context context;
    @Mock
    Activity activity;

    @Mock
    ContextProcessor contextProcessor;
    @Mock
    LocationListener locationListener;

    @Mock
    LocationConfiguration locationConfiguration;
    @Mock
    GooglePlayServicesConfiguration googlePlayServicesConfiguration;
    @Mock
    FallbackListener fallbackListener;

    private GooglePlayServicesLocationProvider googlePlayServicesLocationProvider;

    @Before
    public void setUp() {
        MockitoAnnotations.initMocks(this);

        googlePlayServicesLocationProvider = spy(new GooglePlayServicesLocationProvider(fallbackListener));
        googlePlayServicesLocationProvider.configure(contextProcessor, locationConfiguration, locationListener);
        googlePlayServicesLocationProvider.setDispatcherLocationSource(mockedSource);

        when(locationConfiguration.googlePlayServicesConfiguration()).thenReturn(googlePlayServicesConfiguration);
        when(contextProcessor.getContext()).thenReturn(context);
        when(contextProcessor.getActivity()).thenReturn(activity);
    }

    @Test
    public void onResumeShouldNotRequestLocationUpdateWhenSettingsDialogIsOnTrue() {
        makeSettingsDialogIsOnTrue();

        googlePlayServicesLocationProvider.onResume();

        verify(mockedSource, never()).requestLocationUpdate();
    }

    @Test
    public void onResumeShouldNotRequestLocationUpdateWhenLocationIsAlreadyProvidedAndNotRequiredToKeepTracking() {
        when(locationConfiguration.keepTracking()).thenReturn(false);

        googlePlayServicesLocationProvider.onResume();

        verify(mockedSource, never()).requestLocationUpdate();
    }

    @Test
    public void onResumeShouldRequestLocationUpdateWhenLocationIsNotYetProvided() {
        FakeSimpleTask<Location> lastLocationTask = new FakeSimpleTask<>();
        lastLocationTask.success(null);

        googlePlayServicesLocationProvider.setWaiting(true);
        googlePlayServicesLocationProvider.onLastKnowLocationTaskReceived(lastLocationTask);

        verify(mockedSource).requestLocationUpdate();
    }

    @Test
    public void onResumeShouldRequestLocationUpdateWhenLocationIsAlreadyProvidedButRequiredToKeepTracking() {
        googlePlayServicesLocationProvider.setWaiting(true);
        when(locationConfiguration.keepTracking()).thenReturn(true);

        googlePlayServicesLocationProvider.onResume();

        verify(mockedSource).requestLocationUpdate();
    }

    @Test
    public void onPauseShouldNotRemoveLocationUpdatesWhenSettingsDialogIsOnTrue() {
        makeSettingsDialogIsOnTrue();

        googlePlayServicesLocationProvider.onPause();

        verify(mockedSource, never()).requestLocationUpdate();
        verify(mockedSource, never()).removeLocationUpdates();
    }

    @Test
    public void onPauseShouldRemoveLocationUpdates() {
        googlePlayServicesLocationProvider.onPause();

        verify(mockedSource).removeLocationUpdates();
    }

    @Test
    public void onDestroyShouldRemoveLocationUpdates() {
        googlePlayServicesLocationProvider.onDestroy();

        verify(mockedSource).removeLocationUpdates();
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
    public void getShouldRequestLastLocation() {
        googlePlayServicesLocationProvider.get();

        verify(mockedSource).requestLastLocation();
    }

    @Test
    public void getShouldNotRequestLastLocationWhenIgnore() {
        when(googlePlayServicesConfiguration.ignoreLastKnowLocation()).thenReturn(true);

        googlePlayServicesLocationProvider.get();

        verify(mockedSource, never()).requestLastLocation();
    }

    @Test
    public void onLastKnowLocationTaskReceivedShouldNotCallLocationRequiredWhenLastKnowIsReadyAndNoNeedToKeepTracking() {
        when(locationConfiguration.keepTracking()).thenReturn(false);

        FakeSimpleTask<Location> lastLocationTask = new FakeSimpleTask<>();
        lastLocationTask.success(location);

        googlePlayServicesLocationProvider.onLastKnowLocationTaskReceived(lastLocationTask);

        verify(googlePlayServicesLocationProvider, never()).locationRequired();
    }

    @Test
    public void onLastKnowLocationTaskReceivedShouldCallRequestLocationUpdateWhenLastLocationIsNull() {
        when(locationConfiguration.keepTracking()).thenReturn(false);
        when(googlePlayServicesConfiguration.ignoreLastKnowLocation()).thenReturn(false);

        FakeSimpleTask<Location> lastLocationTask = new FakeSimpleTask<>();
        lastLocationTask.success(null);

        googlePlayServicesLocationProvider.onLastKnowLocationTaskReceived(lastLocationTask);

        verify(googlePlayServicesLocationProvider).locationRequired();
        verify(googlePlayServicesLocationProvider).requestLocationUpdate();
        assertThat(googlePlayServicesLocationProvider.isWaiting()).isFalse();
    }

    @Test
    public void onLastKnowLocationTaskReceivedShouldCallLocationRequiredWhenLastKnowIsNotAvailable() {
        FakeSimpleTask<Location> lastLocationTask = new FakeSimpleTask<>();
        lastLocationTask.success(null);

        googlePlayServicesLocationProvider.onLastKnowLocationTaskReceived(lastLocationTask);

        verify(googlePlayServicesLocationProvider).locationRequired();
    }

    @Test
    public void onLastKnowLocationTaskReceivedShouldNotifyOnLocationChangedWhenLocationIsAvailable() {
        FakeSimpleTask<Location> lastLocationTask = new FakeSimpleTask<>();
        lastLocationTask.success(location);

        googlePlayServicesLocationProvider.onLastKnowLocationTaskReceived(lastLocationTask);

        verify(googlePlayServicesLocationProvider).onLocationChanged(location);
    }

    @Test
    public void onLastKnowLocationTaskReceivedShouldInvokeLocationRequiredWhenKeepTrackingIsTrue() {
        when(locationConfiguration.keepTracking()).thenReturn(true);
        FakeSimpleTask<Location> lastLocationTask = new FakeSimpleTask<>();
        lastLocationTask.success(location);

        googlePlayServicesLocationProvider.onLastKnowLocationTaskReceived(lastLocationTask);

        verify(googlePlayServicesLocationProvider).onLocationChanged(location);
        verify(googlePlayServicesLocationProvider).locationRequired();
    }

    @Test
    public void onLastKnowLocationTaskReceivedShouldInvokeRequestLocationFalseWhenLastKnownLocationIsNull() {
        FakeSimpleTask<Location> lastLocationTask = new FakeSimpleTask<>();
        lastLocationTask.success(null);

        googlePlayServicesLocationProvider.onLastKnowLocationTaskReceived(lastLocationTask);

        verify(googlePlayServicesLocationProvider).locationRequired();
    }

    @Test
    public void cancelShouldRemoveLocationRequestWhenInvokeCancel() {
        googlePlayServicesLocationProvider.cancel();

        verify(mockedSource).removeLocationUpdates();
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
    public void onLocationChangedShouldNotifyListener() {
        googlePlayServicesLocationProvider.onLocationChanged(location);

        verify(locationListener).onLocationChanged(location);
        verify(mockedSource).removeLocationUpdates();
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

        googlePlayServicesLocationProvider.onLocationChanged(location);

        verify(mockedSource).removeLocationUpdates();
    }

    @Test
    public void onLocationChangedShouldNotRemoveUpdateLocationWhenKeepTrackingIsRequired() {
        when(locationConfiguration.keepTracking()).thenReturn(true);

        googlePlayServicesLocationProvider.onLocationChanged(location);

        verify(mockedSource, never()).removeLocationUpdates();
    }

    @Test
    public void onLocationResultShouldCallOnLocationChangedWhenLocationListIsNotEmpty() {
        List<Location> locations = new ArrayList<>();

        locations.add(new Location("1"));
        locations.add(new Location("2"));

        LocationResult locationResult = LocationResult.create(locations);

        googlePlayServicesLocationProvider.onLocationResult(locationResult);

        verify(googlePlayServicesLocationProvider, atLeastOnce()).onLocationChanged(any(Location.class));

        verify(locationListener, atLeastOnce()).onLocationChanged(any(Location.class));
    }

    @Test
    public void onLocationResultShouldNotCallOnLocationChangedWhenLocationListIsEmpty() {
        List<Location> locations = new ArrayList<>();

        LocationResult locationResult = LocationResult.create(locations);

        googlePlayServicesLocationProvider.onLocationResult(locationResult);

        verify(googlePlayServicesLocationProvider, never()).onLocationChanged(any(Location.class));
    }

    @Test
    public void onLocationResultShouldNotCallOnLocationChangedWhenLocationResultIsNull() {
        googlePlayServicesLocationProvider.onLocationResult(null);

        verify(googlePlayServicesLocationProvider, never()).onLocationChanged(any(Location.class));
    }

    @Test
    public void onResultShouldCallRequestLocationUpdateWhenSuccess() {
        googlePlayServicesLocationProvider.onSuccess(getSettingsResultWithSuccess(LocationSettingsStatusCodes.SUCCESS));

        verify(googlePlayServicesLocationProvider).requestLocationUpdate();
    }

    @Test
    public void onResultShouldCallSettingsApiFailWhenChangeUnavailable() {
        googlePlayServicesLocationProvider
                .onFailure(getSettingsResultWithError(LocationSettingsStatusCodes.SETTINGS_CHANGE_UNAVAILABLE));

        verify(googlePlayServicesLocationProvider).settingsApiFail(FailType.GOOGLE_PLAY_SERVICES_SETTINGS_DIALOG);
    }

    @Test
    public void onResultShouldCallResolveSettingsApiWhenResolutionRequired() {
        Exception settingsResultWith = getSettingsResultWithError(LocationSettingsStatusCodes.RESOLUTION_REQUIRED);
        googlePlayServicesLocationProvider.onFailure(settingsResultWith);

        verify(googlePlayServicesLocationProvider).resolveSettingsApi((any(ResolvableApiException.class)));
    }

    @Test
    public void onResultShouldCallSettingsApiFailWithSettingsDeniedWhenOtherCase() {
        Exception settingsResultWith = getSettingsResultWithError(LocationSettingsStatusCodes.CANCELED);
        googlePlayServicesLocationProvider.onFailure(settingsResultWith);

        verify(googlePlayServicesLocationProvider).settingsApiFail(FailType.GOOGLE_PLAY_SERVICES_SETTINGS_DENIED);
    }

    @Test
    public void resolveSettingsApiShouldCallSettingsApiFailWhenThereIsNoActivity() {
        when(contextProcessor.getActivity()).thenReturn(null);

        googlePlayServicesLocationProvider.resolveSettingsApi(new ResolvableApiException(new Status(1)));

        verify(googlePlayServicesLocationProvider).settingsApiFail(FailType.VIEW_NOT_REQUIRED_TYPE);
    }

    @Test
    public void resolveSettingsApiShouldStartSettingsApiResolutionForResult() throws Exception {
        Status status = new Status(1);
        ResolvableApiException resolvable = new ResolvableApiException(status);

        googlePlayServicesLocationProvider.resolveSettingsApi(resolvable);

        verify(mockedSource).startSettingsApiResolutionForResult(resolvable, activity);
    }

    @Test
    public void resolveSettingsApiShouldCallSettingsApiFailWhenExceptionThrown() throws Exception {
        Status status = new Status(1);
        ResolvableApiException resolvable = new ResolvableApiException(status);

        doThrow(new SendIntentException()).when(mockedSource).startSettingsApiResolutionForResult(resolvable, activity);

        googlePlayServicesLocationProvider.resolveSettingsApi(resolvable);

        verify(googlePlayServicesLocationProvider).settingsApiFail(FailType.GOOGLE_PLAY_SERVICES_SETTINGS_DIALOG);
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
    public void requestLocationUpdateShouldRequest() {
        googlePlayServicesLocationProvider.requestLocationUpdate();

        verify(mockedSource).requestLocationUpdate();
    }

    @Test
    public void settingsApiFailShouldCallFailWhenConfigurationFailOnSettingsApiSuspendedTrue() {
        when(googlePlayServicesConfiguration.failOnSettingsApiSuspended()).thenReturn(true);

        googlePlayServicesLocationProvider.settingsApiFail(FailType.UNKNOWN);

        verify(googlePlayServicesLocationProvider).failed(FailType.UNKNOWN);
    }

    @Test
    public void settingsApiFailShouldCallRequestLocationUpdateWhenConfigurationFailOnSettingsApiSuspendedFalse() {
        when(googlePlayServicesConfiguration.failOnSettingsApiSuspended()).thenReturn(false);

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
        googlePlayServicesLocationProvider.onFailure(getSettingsResultWithError(LocationSettingsStatusCodes.RESOLUTION_REQUIRED));
    }

    @NonNull
    private static LocationSettingsResponse getSettingsResultWithSuccess(int statusCode) {
        Status status = new Status(statusCode, null, null);

        LocationSettingsResponse result = new LocationSettingsResponse();

        result.setResult(new LocationSettingsResult(status));

        return result;
    }

    @NonNull
    private static Exception getSettingsResultWithError(int statusCode) {
        Status status = new Status(statusCode, null, null);

        if (statusCode == LocationSettingsStatusCodes.RESOLUTION_REQUIRED) {
            return new ResolvableApiException(status);
        } else {
            return new ApiException(status);
        }
    }
}