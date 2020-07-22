package com.yayandroid.locationmanager.providers.locationprovider;

import android.app.Activity;
import android.content.Context;
import android.content.IntentSender.SendIntentException;
import android.location.Location;

import androidx.annotation.NonNull;

import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.common.api.ResolvableApiException;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.Helper;
import com.google.android.gms.location.LocationAvailability;
import com.google.android.gms.location.LocationSettingsResponse;
import com.google.android.gms.location.LocationSettingsStatusCodes;
import com.google.android.gms.tasks.Task;
import com.yayandroid.locationmanager.configuration.GooglePlayServicesConfiguration;
import com.yayandroid.locationmanager.configuration.LocationConfiguration;
import com.yayandroid.locationmanager.constants.FailType;
import com.yayandroid.locationmanager.constants.ProcessType;
import com.yayandroid.locationmanager.constants.RequestCode;
import com.yayandroid.locationmanager.listener.FallbackListener;
import com.yayandroid.locationmanager.listener.LocationListener;
import com.yayandroid.locationmanager.mocks.MockLocationSettingsResponseTask;
import com.yayandroid.locationmanager.mocks.MockSimpleTask;
import com.yayandroid.locationmanager.view.ContextProcessor;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
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
        when(mockedSource.getLocationAvailability()).thenReturn(new MockSimpleTask<>(Helper.getLocationAvailability(false)));

        googlePlayServicesLocationProvider.setWaiting(true);

        googlePlayServicesLocationProvider.onResume();

        verify(mockedSource).requestLocationUpdate();
    }

    @Test
    public void onResumeShouldRequestLocationUpdateWhenLocationIsAlreadyProvidedButRequiredToKeepTracking() {
        googlePlayServicesLocationProvider.setWaiting(true);
        when(locationConfiguration.keepTracking()).thenReturn(true);

        when(mockedSource.getLocationAvailability()).thenReturn(new MockSimpleTask<>(Helper.getLocationAvailability(false)));

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
        when(mockedSource.removeLocationUpdates()).thenReturn(new MockSimpleTask<>(((Void) null)));

        googlePlayServicesLocationProvider.onPause();

        verify(mockedSource).removeLocationUpdates();
    }

    @Test
    public void onDestroyShouldRemoveLocationUpdates() {
        when(mockedSource.removeLocationUpdates()).thenReturn(new MockSimpleTask<>(((Void) null)));

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

        when(mockedSource.getLocationAvailability()).thenReturn(new MockSimpleTask<>(Helper.getLocationAvailability(false)));

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
    public void getShouldRequestLocationUpdate() {
        when(mockedSource.getLocationAvailability()).thenReturn(new MockSimpleTask<>(Helper.getLocationAvailability(false)));

        googlePlayServicesLocationProvider.get();

        verify(mockedSource).requestLocationUpdate();
    }

    @Test
    public void cancelShouldRemoveLocationRequestWhenInvokeCancel() {
        when(mockedSource.removeLocationUpdates()).thenReturn(new MockSimpleTask<>(((Void) null)));

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
    public void onConnectedShouldNotCheckLastKnowLocationWhenRequirementsIgnore() {
        when(googlePlayServicesConfiguration.ignoreLastKnowLocation()).thenReturn(true);

        googlePlayServicesLocationProvider.onConnected();

        verify(googlePlayServicesLocationProvider, never()).checkLastKnowLocation();
    }

    @Test
    public void onConnectedShouldCheckLastKnowLocation() {
        when(mockedSource.getLocationAvailability()).thenReturn(new MockSimpleTask<>(Helper.getLocationAvailability(true)));
        when(mockedSource.getLastLocation()).thenReturn(new MockSimpleTask<>(new Location("")));
        when(mockedSource.removeLocationUpdates()).thenReturn(new MockSimpleTask<>(((Void) null)));

        googlePlayServicesLocationProvider.onConnected();

        verify(googlePlayServicesLocationProvider).checkLastKnowLocation();
    }

    @Test
    public void onConnectedShouldNotCallLocationRequiredWhenLastKnowIsReadyAndNoNeedToKeepTracking() {
        when(mockedSource.getLocationAvailability()).thenReturn(new MockSimpleTask<>(Helper.getLocationAvailability(true)));
        when(mockedSource.getLastLocation()).thenReturn(new MockSimpleTask<>(new Location("")));
        when(mockedSource.removeLocationUpdates()).thenReturn(new MockSimpleTask<>(((Void) null)));
        when(locationConfiguration.keepTracking()).thenReturn(false);

        googlePlayServicesLocationProvider.onConnected();

        verify(googlePlayServicesLocationProvider, never()).locationRequired();
    }

    @Test
    public void onConnectedShouldSwitchMRequestingLocationUpdatesToTrue() {
        // Have first condition false
        when(locationConfiguration.keepTracking()).thenReturn(false);

        // Have second condition false
        when(googlePlayServicesConfiguration.ignoreLastKnowLocation()).thenReturn(false);
        when(mockedSource.getLocationAvailability()).thenReturn(new MockSimpleTask<>(Helper.getLocationAvailability(true)));
        when(mockedSource.getLastLocation()).thenReturn(new MockSimpleTask<>(((Location) null)));

        // mRequestingLocationUpdates is false on start

        googlePlayServicesLocationProvider.onConnected();

        verify(googlePlayServicesLocationProvider).locationRequired();
        assertThat(googlePlayServicesLocationProvider.mRequestingLocationUpdates).isTrue();
    }

    @Test
    public void onConnectedShouldCallLocationRequiredWhenLastKnowIsNotAvailable() {
        when(mockedSource.getLocationAvailability()).thenReturn(new MockSimpleTask<>(Helper.getLocationAvailability(false)));

        googlePlayServicesLocationProvider.onConnected();

        verify(googlePlayServicesLocationProvider).locationRequired();
    }

    @Test
    public void onConnectedShouldCallLocationRequiredWhenConfigurationRequiresKeepTracking() {
        when(mockedSource.getLocationAvailability()).thenReturn(new MockSimpleTask<>(Helper.getLocationAvailability(true)));
        when(mockedSource.getLastLocation()).thenReturn(new MockSimpleTask<>(location));
        when(locationConfiguration.keepTracking()).thenReturn(true);

        googlePlayServicesLocationProvider.onConnected();

        verify(googlePlayServicesLocationProvider).locationRequired();
    }

    @Test
    public void onLocationChangedShouldNotifyListener() {
        when(mockedSource.removeLocationUpdates()).thenReturn(new MockSimpleTask<>(((Void) null)));

        googlePlayServicesLocationProvider.onLocationChanged(location);

        verify(locationListener).onLocationChanged(location);
        verify(mockedSource).removeLocationUpdates();
    }

    @Test
    public void onLocationChangedShouldSetWaitingFalse() {
        when(mockedSource.removeLocationUpdates()).thenReturn(new MockSimpleTask<>(((Void) null)));

        googlePlayServicesLocationProvider.setWaiting(true);
        assertThat(googlePlayServicesLocationProvider.isWaiting()).isTrue();

        googlePlayServicesLocationProvider.onLocationChanged(location);

        assertThat(googlePlayServicesLocationProvider.isWaiting()).isFalse();
    }

    @Test
    public void onLocationChangedShouldRemoveUpdateLocationWhenKeepTrackingIsNotRequired() {
        when(locationConfiguration.keepTracking()).thenReturn(false);
        when(mockedSource.removeLocationUpdates()).thenReturn(new MockSimpleTask<>(((Void) null)));

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
    public void onResultShouldCallRequestLocationUpdateWhenSuccess() {
        googlePlayServicesLocationProvider.onResult(getSettingsResultWith(LocationSettingsStatusCodes.SUCCESS, false));

        verify(googlePlayServicesLocationProvider).requestLocationUpdate();
    }

    @Test
    public void onResultShouldCallSettingsApiFailWhenChangeUnavailable() {
        googlePlayServicesLocationProvider
                .onResult(getSettingsResultWith(LocationSettingsStatusCodes.SETTINGS_CHANGE_UNAVAILABLE, true));

        verify(googlePlayServicesLocationProvider).settingsApiFail(FailType.GOOGLE_PLAY_SERVICES_SETTINGS_DIALOG);
    }

    @Test
    public void onResultShouldCallResolveSettingsApiWhenResolutionRequired() {
        Task<LocationSettingsResponse> settingsResultWith = getSettingsResultWith(LocationSettingsStatusCodes.RESOLUTION_REQUIRED, true);
        googlePlayServicesLocationProvider.onResult(settingsResultWith);

        verify(googlePlayServicesLocationProvider).resolveSettingsApi((any(ResolvableApiException.class)));
    }

    @Test
    public void onResultShouldCallSettingsApiFailWithSettingsDeniedWhenOtherCase() {
        Task<LocationSettingsResponse> settingsResultWith = getSettingsResultWith(LocationSettingsStatusCodes.CANCELED, true);
        googlePlayServicesLocationProvider.onResult(settingsResultWith);

        verify(googlePlayServicesLocationProvider).settingsApiFail(FailType.GOOGLE_PLAY_SERVICES_SETTINGS_DENIED);
    }

    @Test
    public void onResultShouldDoNothingWhenClassCastExceptionThrows() {
        Status status = new Status(LocationSettingsStatusCodes.RESOLUTION_REQUIRED, null, null);

        ApiException error = new ApiException(status);

        Task<LocationSettingsResponse> settingsResultWith = new MockLocationSettingsResponseTask(error);

        googlePlayServicesLocationProvider.onResult(settingsResultWith);

        verify(googlePlayServicesLocationProvider, never()).requestLocationUpdate();

        verify(googlePlayServicesLocationProvider, never()).resolveSettingsApi(any(ResolvableApiException.class));

        verify(googlePlayServicesLocationProvider, never()).getSourceProvider();
        verify(googlePlayServicesLocationProvider, never()).settingsApiFail(FailType.VIEW_NOT_REQUIRED_TYPE);
        verify(googlePlayServicesLocationProvider, never()).settingsApiFail(FailType.GOOGLE_PLAY_SERVICES_SETTINGS_DIALOG);

        verify(googlePlayServicesLocationProvider, never()).settingsApiFail(FailType.GOOGLE_PLAY_SERVICES_SETTINGS_DENIED);
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
    public void checkLastKnowLocationShouldReturnFalseWhenLocationIsNotAvailable() {
        when(mockedSource.getLocationAvailability()).thenReturn(new MockSimpleTask<>(Helper.getLocationAvailability(false)));

        googlePlayServicesLocationProvider.checkLastKnowLocation();

        verify(mockedSource, never()).getLastLocation();
    }

    @Test
    public void checkLastKnowLocationShouldReturnFalseWhenLocationIsNull() {
        when(mockedSource.getLocationAvailability()).thenReturn(new MockSimpleTask<>(Helper.getLocationAvailability(false)));

        googlePlayServicesLocationProvider.checkLastKnowLocation();

        verify(mockedSource, never()).getLastLocation();
    }

    @Test
    public void checkLastKnowLocationShouldReturnTrueWhenLocationIsAvailable() {
        when(mockedSource.getLocationAvailability()).thenReturn(new MockSimpleTask<>(Helper.getLocationAvailability(true)));
        when(mockedSource.getLastLocation()).thenReturn(new MockSimpleTask<>(location));
        when(mockedSource.removeLocationUpdates()).thenReturn(new MockSimpleTask<>(((Void) null)));

        googlePlayServicesLocationProvider.checkLastKnowLocation();

        verify(mockedSource).getLastLocation();
    }

    @Test
    public void checkLastKnowLocationShouldInvokeRequestLocationFalseWhenLastKnownLocationIsNull() {
        when(mockedSource.getLocationAvailability()).thenReturn(new MockSimpleTask<>(Helper.getLocationAvailability(true)));
        when(mockedSource.getLastLocation()).thenReturn(new MockSimpleTask<>(((Location) null)));

        googlePlayServicesLocationProvider.checkLastKnowLocation();

        verify(googlePlayServicesLocationProvider).requestLocation(false);
    }

    @Test
    public void checkLastKnowLocationShouldInvokeRequestLocationFalseWhenGetLocationAvailabilityFail() {
        when(mockedSource.getLocationAvailability()).thenReturn(new MockSimpleTask<LocationAvailability>(new NullPointerException("test fail")));

        googlePlayServicesLocationProvider.checkLastKnowLocation();

        verify(googlePlayServicesLocationProvider).requestLocation(false);
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

    @Test
    public void onConnectedShouldNotRequestLocationWhenRequestingLocationUpdatesTrue() {
        googlePlayServicesLocationProvider.mRequestingLocationUpdates = true;

        googlePlayServicesLocationProvider.onConnected();

        verify(googlePlayServicesLocationProvider, never()).requestLocation(false);
        verify(googlePlayServicesLocationProvider, never()).checkLastKnowLocation();
    }

    private void makeSettingsDialogIsOnTrue() {
        googlePlayServicesLocationProvider.onResult(getSettingsResultWith(LocationSettingsStatusCodes.RESOLUTION_REQUIRED, true));
    }

    @NonNull
    private Task<LocationSettingsResponse> getSettingsResultWith(int statusCode, boolean isError) {
        return new MockLocationSettingsResponseTask(statusCode, isError);
    }
}