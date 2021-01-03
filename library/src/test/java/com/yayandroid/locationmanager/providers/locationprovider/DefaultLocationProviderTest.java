package com.yayandroid.locationmanager.providers.locationprovider;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.location.Location;
import android.location.LocationManager;

import com.yayandroid.locationmanager.configuration.DefaultProviderConfiguration;
import com.yayandroid.locationmanager.configuration.LocationConfiguration;
import com.yayandroid.locationmanager.constants.FailType;
import com.yayandroid.locationmanager.constants.ProcessType;
import com.yayandroid.locationmanager.constants.RequestCode;
import com.yayandroid.locationmanager.helper.UpdateRequest;
import com.yayandroid.locationmanager.helper.continuoustask.ContinuousTask;
import com.yayandroid.locationmanager.listener.LocationListener;
import com.yayandroid.locationmanager.providers.dialogprovider.DialogProvider;
import com.yayandroid.locationmanager.view.ContextProcessor;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class DefaultLocationProviderTest {

    private static final String GPS_PROVIDER = LocationManager.GPS_PROVIDER;
    private static final String NETWORK_PROVIDER = LocationManager.NETWORK_PROVIDER;
    private static final Location DUMMY_LOCATION = new Location("");

    @Mock ContextProcessor contextProcessor;
    @Mock LocationListener locationListener;

    @Mock Activity activity;
    @Mock Context context;
    @Mock Dialog dialog;

    @Mock ContinuousTask continuousTask;
    @Mock UpdateRequest updateRequest;

    @Mock LocationConfiguration locationConfiguration;
    @Mock DefaultProviderConfiguration defaultProviderConfiguration;
    @Mock DialogProvider dialogProvider;

    @Mock DefaultLocationSource defaultLocationSource;

    private DefaultLocationProvider defaultLocationProvider;

    @Before
    public void setUp() {
        MockitoAnnotations.initMocks(this);

        when(locationConfiguration.defaultProviderConfiguration()).thenReturn(defaultProviderConfiguration);
        when(defaultProviderConfiguration.gpsDialogProvider()).thenReturn(dialogProvider);
        when(dialogProvider.getDialog(any(Context.class))).thenReturn(dialog);

        when(contextProcessor.getContext()).thenReturn(context);
        when(contextProcessor.getActivity()).thenReturn(activity);

        when(defaultLocationSource.getProviderSwitchTask()).thenReturn(continuousTask);
        when(defaultLocationSource.getUpdateRequest()).thenReturn(updateRequest);

        defaultLocationProvider = spy(new DefaultLocationProvider());
        defaultLocationProvider.setDefaultLocationSource(defaultLocationSource);
        defaultLocationProvider.configure(contextProcessor, locationConfiguration, locationListener);
    }

    @Test
    public void configureShouldInvokeInitialize() {
        DefaultLocationProvider provider = spy(new DefaultLocationProvider());
        provider.configure(defaultLocationProvider);

        verify(provider).initialize();
    }

    @Test
    public void onDestroyShouldRemoveInstances() {
        defaultLocationProvider.onDestroy();

        verify(defaultLocationSource).removeLocationUpdates(defaultLocationProvider);
        verify(defaultLocationSource).removeSwitchTask();
        verify(defaultLocationSource).removeUpdateRequest();
    }

    @Test
    public void cancelShouldStopTasks() {
        defaultLocationProvider.cancel();

        verify(updateRequest).release();
        verify(continuousTask).stop();
    }

    @Test
    public void onPauseShouldPauseTasks() {
        defaultLocationProvider.onPause();

        verify(updateRequest).release();
        verify(continuousTask).pause();
    }

    @Test
    public void onResumeShouldResumeUpdateRequest() {
        defaultLocationProvider.onResume();

        verify(updateRequest).run();
    }

    @Test
    public void onResumeShouldResumeSwitchTaskWhenLocationIsStillRequired() {
        defaultLocationProvider.setWaiting(true);
        defaultLocationProvider.onResume();

        verify(continuousTask).resume();
    }

    @Test
    public void onResumeDialogShouldDismissWhenDialogIsOnAndGPSIsActivated() {
        defaultLocationProvider.askForEnableGPS(); // to get dialog initialized
        when(dialog.isShowing()).thenReturn(true);
        enableLocationProvider();

        defaultLocationProvider.onResume();

        verify(dialog).dismiss();
        verify(defaultLocationProvider).onGPSActivated();
    }

    @Test
    public void onResumeDialogShouldNotDismissedWhenGPSNotActivated() {
        defaultLocationProvider.askForEnableGPS(); // to get dialog initialized
        when(dialog.isShowing()).thenReturn(true);
        disableLocationProvider();

        defaultLocationProvider.onResume();

        verify(dialog, never()).dismiss();
    }

    @Test
    public void isDialogShowingShouldReturnFalseWhenGPSDialogIsNotOn() {
        assertThat(defaultLocationProvider.isDialogShowing()).isFalse();
    }

    @Test
    public void isDialogShowingShouldReturnTrueWhenGPSDialogIsOn() {
        defaultLocationProvider.askForEnableGPS(); // to get dialog initialized
        when(dialog.isShowing()).thenReturn(true);

        assertThat(defaultLocationProvider.isDialogShowing()).isTrue();
    }

    @Test
    public void onActivityResultShouldCallOnGPSActivated() {
        enableLocationProvider();
        defaultLocationProvider.onActivityResult(RequestCode.GPS_ENABLE, -1, null);

        verify(defaultLocationProvider).onGPSActivated();
    }

    @Test
    public void onActivityResultShouldCallGetLocationByNetworkWhenGPSIsNotEnabled() {
        disableLocationProvider();
        defaultLocationProvider.onActivityResult(RequestCode.GPS_ENABLE, -1, null);

        verify(defaultLocationProvider).getLocationByNetwork();
    }

    @Test
    public void getShouldSetWaitingTrue() {
        enableLocationProvider();
        assertThat(defaultLocationProvider.isWaiting()).isFalse();

        defaultLocationProvider.get();

        assertThat(defaultLocationProvider.isWaiting()).isTrue();
    }

    @Test
    public void getShouldAskForLocationWithGPSProviderWhenItIsEnabled() {
        enableLocationProvider();

        defaultLocationProvider.get();

        verify(defaultLocationProvider).askForLocation(GPS_PROVIDER);
    }

    @Test
    public void getShouldAskForEnableGPSWhenGPSIsNotEnabledButRequiredByConfigurationToAskForIt() {
        disableLocationProvider();
        when(defaultProviderConfiguration.askForEnableGPS()).thenReturn(true);

        defaultLocationProvider.get();

        verify(defaultLocationProvider).askForEnableGPS();
    }

    @Test
    public void getShouldGetLocationByNetworkWhenGPSNotRequiredToAsk() {
        disableLocationProvider();
        when(defaultProviderConfiguration.askForEnableGPS()).thenReturn(false);

        defaultLocationProvider.get();

        verify(defaultLocationProvider).getLocationByNetwork();
    }

    @Test
    public void getShouldGetLocationByNetworkWhenGPSNotEnabledAndThereIsNoActivity() {
        disableLocationProvider();
        when(defaultProviderConfiguration.askForEnableGPS()).thenReturn(true);
        when(contextProcessor.getActivity()).thenReturn(null);

        defaultLocationProvider.get();

        verify(defaultLocationProvider).getLocationByNetwork();
    }

    @Test
    public void askForEnableGPSShouldShowDialog() {
        defaultLocationProvider.askForEnableGPS();

        verify(dialogProvider).setDialogListener(defaultLocationProvider);
        verify(dialogProvider).getDialog(activity);
        verify(dialog).show();
    }

    @Test
    public void onGPSActivatedShouldAskForLocation() {
        defaultLocationProvider.onGPSActivated();

        verify(defaultLocationProvider).askForLocation(GPS_PROVIDER);
    }

    @Test
    public void getLocationByNetworkShouldAskForLocationWhenNetworkIsAvailable() {
        enableLocationProvider();

        defaultLocationProvider.getLocationByNetwork();

        verify(defaultLocationProvider).askForLocation(LocationManager.NETWORK_PROVIDER);
    }

    @Test
    public void getLocationByNetworkShouldFailWhenNetworkIsNotAvailable() {
        disableLocationProvider();

        defaultLocationProvider.getLocationByNetwork();

        verify(locationListener).onLocationFailed(FailType.NETWORK_NOT_AVAILABLE);
    }

    @Test
    public void askForLocationShouldStopSwitchTasks() {
        defaultLocationProvider.askForLocation(GPS_PROVIDER);

        verify(continuousTask).stop();
    }

    @Test
    public void askForLocationShouldCheckLastKnowLocation() {
        defaultLocationProvider.askForLocation(GPS_PROVIDER);

        verify(defaultLocationProvider).checkForLastKnowLocation();
    }

    @Test
    public void askForLocationShouldNotifyProcessChangeRequestLocationUpdateDelayTaskWhenLastLocationIsNotSufficient() {
        final long ONE_SECOND = 1000;
        when(defaultLocationSource.getProviderSwitchTask()).thenReturn(continuousTask);
        when(defaultProviderConfiguration.gpsWaitPeriod()).thenReturn(ONE_SECOND);

        defaultLocationProvider.askForLocation(GPS_PROVIDER);

        verify(defaultLocationProvider).notifyProcessChange();
        verify(defaultLocationProvider).requestUpdateLocation();
        verify(continuousTask).delayed(ONE_SECOND);
    }

    @Test
    public void askForLocationShouldNotifyProcessChangeAndRequestLocationUpdateWhenKeepTrackingIsTrue() {
        Location location = new Location(GPS_PROVIDER);
        when(defaultProviderConfiguration.acceptableAccuracy()).thenReturn(1F);
        when(defaultProviderConfiguration.acceptableTimePeriod()).thenReturn(1L);
        when(defaultLocationSource.getLastKnownLocation(GPS_PROVIDER)).thenReturn(location);
        when(defaultLocationSource.isLocationSufficient(location, 1L, 1F)).thenReturn(true);
        when(locationConfiguration.keepTracking()).thenReturn(true);

        defaultLocationProvider.askForLocation(GPS_PROVIDER);

        verify(defaultLocationProvider).notifyProcessChange();
        verify(defaultLocationProvider).requestUpdateLocation();
    }

    @Test
    public void checkForLastKnownLocationShouldReturnFalse() {
        assertThat(defaultLocationProvider.checkForLastKnowLocation()).isFalse();
    }

    @Test
    public void checkForLastKnownLocationShouldCallOnLocationReceivedAndReturnTrueWhenSufficient() {
        defaultLocationProvider.setCurrentProvider(GPS_PROVIDER);
        Location location = new Location(GPS_PROVIDER);
        when(defaultProviderConfiguration.acceptableAccuracy()).thenReturn(1F);
        when(defaultProviderConfiguration.acceptableTimePeriod()).thenReturn(1L);
        when(defaultLocationSource.getLastKnownLocation(GPS_PROVIDER)).thenReturn(location);
        when(defaultLocationSource.isLocationSufficient(location, 1L, 1F)).thenReturn(true);

        assertThat(defaultLocationProvider.checkForLastKnowLocation()).isTrue();
        verify(locationListener).onLocationChanged(location);
    }

    @Test
    public void notifyProcessChangeShouldNotifyWithCorrespondingTypeForProvider() {
        defaultLocationProvider.setCurrentProvider(GPS_PROVIDER);
        defaultLocationProvider.notifyProcessChange();
        verify(locationListener).onProcessTypeChanged(ProcessType.GETTING_LOCATION_FROM_GPS_PROVIDER);

        defaultLocationProvider.setCurrentProvider(NETWORK_PROVIDER);
        defaultLocationProvider.notifyProcessChange();
        verify(locationListener).onProcessTypeChanged(ProcessType.GETTING_LOCATION_FROM_NETWORK_PROVIDER);
    }

    @Test
    public void requestUpdateLocationShouldRunUpdateLocationTaskWithCurrentProvider() {
        long timeInterval = 100;
        long distanceInterval = 200;
        when(defaultProviderConfiguration.requiredTimeInterval()).thenReturn(timeInterval);
        when(defaultProviderConfiguration.requiredDistanceInterval()).thenReturn(distanceInterval);

        defaultLocationProvider.setCurrentProvider(GPS_PROVIDER);
        defaultLocationProvider.requestUpdateLocation();

        verify(updateRequest).run(GPS_PROVIDER, timeInterval, distanceInterval);
    }

    @Test
    public void getWaitPeriodShouldReturnCorrespondingTimeForProvider() {
        long gpsWaitPeriod = 100;
        long networkWaitPeriod = 200;
        when(defaultProviderConfiguration.gpsWaitPeriod()).thenReturn(gpsWaitPeriod);
        when(defaultProviderConfiguration.networkWaitPeriod()).thenReturn(networkWaitPeriod);

        defaultLocationProvider.setCurrentProvider(GPS_PROVIDER);
        assertThat(defaultLocationProvider.getWaitPeriod()).isEqualTo(gpsWaitPeriod);

        defaultLocationProvider.setCurrentProvider(NETWORK_PROVIDER);
        assertThat(defaultLocationProvider.getWaitPeriod()).isEqualTo(networkWaitPeriod);
    }

    @Test
    public void onLocationReceivedShouldNotifyListenerAndSetWaitingFalse() {
        defaultLocationProvider.setWaiting(true);

        defaultLocationProvider.onLocationReceived(DUMMY_LOCATION);

        verify(locationListener).onLocationChanged(DUMMY_LOCATION);
        assertThat(defaultLocationProvider.isWaiting()).isFalse();
    }

    @Test
    public void onLocationFailedShouldNotifyListenerAndSetWaitingFalse() {
        defaultLocationProvider.setWaiting(true);

        defaultLocationProvider.onLocationFailed(FailType.UNKNOWN);

        verify(locationListener).onLocationFailed(FailType.UNKNOWN);
        assertThat(defaultLocationProvider.isWaiting()).isFalse();
    }

    @Test
    public void onLocationChangedShouldPassLocationToReceived() {
        defaultLocationProvider.onLocationChanged(DUMMY_LOCATION);

        verify(defaultLocationProvider).onLocationReceived(DUMMY_LOCATION);
    }

    @Test
    public void onLocationChangedShouldStopSwitchTask() {
        defaultLocationProvider.onLocationChanged(DUMMY_LOCATION);

        verify(continuousTask).stop();
    }

    @Test
    public void onLocationChangedShouldNotStopSwitchTaskIfSwitchTaskIsRemoved() {
        when(defaultLocationSource.switchTaskIsRemoved()).thenReturn(true);

        defaultLocationProvider.onLocationChanged(DUMMY_LOCATION);
        verify(continuousTask, never()).stop();
    }

    @Test
    public void onLocationChangedShouldReturnIfUpdateRequestIsRemoved() {
        when(defaultLocationSource.updateRequestIsRemoved()).thenReturn(true);

        defaultLocationProvider.onLocationChanged(DUMMY_LOCATION);

        verify(defaultLocationProvider, never()).onLocationReceived(DUMMY_LOCATION);
    }

    @Test
    public void onLocationChangedShouldRemoveUpdatesWhenKeepTrackingFalse() {
        when(locationConfiguration.keepTracking()).thenReturn(false);

        defaultLocationProvider.onLocationChanged(DUMMY_LOCATION);

        verify(defaultLocationSource).removeLocationUpdates(defaultLocationProvider);
        verify(updateRequest).release();
    }

    @Test
    public void onLocationChangedShouldNotRemoveUpdatesWhenKeepTrackingTrue() {
        when(locationConfiguration.keepTracking()).thenReturn(true);

        defaultLocationProvider.onLocationChanged(DUMMY_LOCATION);

        verify(defaultLocationSource, never()).removeLocationUpdates(defaultLocationProvider);
        verify(updateRequest, never()).release();
    }

    @Test
    public void onStatusChangedShouldRedirectToListener() {
        defaultLocationProvider.onStatusChanged(GPS_PROVIDER, 1, null);

        verify(locationListener).onStatusChanged(GPS_PROVIDER, 1, null);
    }

    @Test
    public void onProviderEnabledShouldRedirectToListener() {
        defaultLocationProvider.onProviderEnabled(GPS_PROVIDER);

        verify(locationListener).onProviderEnabled(GPS_PROVIDER);
    }

    @Test
    public void onProviderDisabledShouldRedirectToListener() {
        defaultLocationProvider.onProviderDisabled(GPS_PROVIDER);

        verify(locationListener).onProviderDisabled(GPS_PROVIDER);
    }

    @Test
    public void runScheduledTaskShouldReleaseUpdateRequest() {
        defaultLocationProvider.runScheduledTask(DefaultLocationSource.PROVIDER_SWITCH_TASK);

        verify(updateRequest).release();
    }

    @Test
    public void runScheduledTaskShouldGetLocationByNetworkWhenCurrentProviderIsGPS() {
        defaultLocationProvider.setCurrentProvider(GPS_PROVIDER);

        defaultLocationProvider.runScheduledTask(DefaultLocationSource.PROVIDER_SWITCH_TASK);

        verify(defaultLocationProvider).getLocationByNetwork();
    }

    @Test
    public void runScheduledTaskShouldFailWithTimeoutWhenCurrentProviderIsNetwork() {
        defaultLocationProvider.setCurrentProvider(NETWORK_PROVIDER);

        defaultLocationProvider.runScheduledTask(DefaultLocationSource.PROVIDER_SWITCH_TASK);

        verify(locationListener).onLocationFailed(FailType.TIMEOUT);
    }

    @Test
    public void onPositiveButtonClickShouldFailWhenThereIsNoActivityOrFragment() {
        when(contextProcessor.getActivity()).thenReturn(null);

        defaultLocationProvider.onPositiveButtonClick();

        verify(locationListener).onLocationFailed(FailType.VIEW_NOT_REQUIRED_TYPE);
    }

    @Test
    public void onNegativeButtonClickShouldGetLocationByNetwork() {
        defaultLocationProvider.onNegativeButtonClick();

        verify(defaultLocationProvider).getLocationByNetwork();
    }

    private void enableLocationProvider() {
        when(defaultLocationSource.isProviderEnabled(anyString())).thenReturn(true);
    }

    private void disableLocationProvider() {
        when(defaultLocationSource.isProviderEnabled(anyString())).thenReturn(false);
    }

}