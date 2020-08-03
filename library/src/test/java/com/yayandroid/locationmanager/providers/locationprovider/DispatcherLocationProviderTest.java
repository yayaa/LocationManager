package com.yayandroid.locationmanager.providers.locationprovider;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnCancelListener;
import android.content.Intent;

import com.google.android.gms.common.ConnectionResult;
import com.yayandroid.locationmanager.configuration.DefaultProviderConfiguration;
import com.yayandroid.locationmanager.configuration.GooglePlayServicesConfiguration;
import com.yayandroid.locationmanager.configuration.LocationConfiguration;
import com.yayandroid.locationmanager.constants.FailType;
import com.yayandroid.locationmanager.constants.RequestCode;
import com.yayandroid.locationmanager.helper.continuoustask.ContinuousTask;
import com.yayandroid.locationmanager.listener.LocationListener;
import com.yayandroid.locationmanager.view.ContextProcessor;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

public class DispatcherLocationProviderTest {

    private final static long GOOGLE_PLAY_SERVICES_SWITCH_PERIOD = 5 * 1000;
    private final static int RESOLVABLE_ERROR = ConnectionResult.SERVICE_MISSING;
    private final static int NOT_RESOLVABLE_ERROR = ConnectionResult.INTERNAL_ERROR;

    @Mock ContextProcessor contextProcessor;
    @Mock LocationListener locationListener;

    @Mock Activity activity;
    @Mock Context context;
    @Mock Dialog dialog;

    @Mock LocationConfiguration locationConfiguration;
    @Mock GooglePlayServicesConfiguration googlePlayServicesConfiguration;
    @Mock DefaultProviderConfiguration defaultProviderConfiguration;

    @Mock DispatcherLocationSource dispatcherLocationSource;
    @Mock DefaultLocationProvider defaultLocationProvider;
    @Mock GooglePlayServicesLocationProvider googlePlayServicesLocationProvider;
    @Mock ContinuousTask continuousTask;

    private DispatcherLocationProvider dispatcherLocationProvider;

    @Before
    public void setUp() {
        MockitoAnnotations.initMocks(this);

        dispatcherLocationProvider = spy(new DispatcherLocationProvider());
        dispatcherLocationProvider.configure(contextProcessor, locationConfiguration, locationListener);
        dispatcherLocationProvider.setDispatcherLocationSource(dispatcherLocationSource);

        when(locationConfiguration.defaultProviderConfiguration()).thenReturn(defaultProviderConfiguration);
        when(locationConfiguration.googlePlayServicesConfiguration()).thenReturn(googlePlayServicesConfiguration);
        when(googlePlayServicesConfiguration.googlePlayServicesWaitPeriod()).thenReturn(GOOGLE_PLAY_SERVICES_SWITCH_PERIOD);

        when(dispatcherLocationSource.createDefaultLocationProvider()).thenReturn(defaultLocationProvider);
        when(dispatcherLocationSource.createGooglePlayServicesLocationProvider(dispatcherLocationProvider))
              .thenReturn(googlePlayServicesLocationProvider);
        when(dispatcherLocationSource.gpServicesSwitchTask()).thenReturn(continuousTask);

        when(contextProcessor.getContext()).thenReturn(context);
        when(contextProcessor.getActivity()).thenReturn(activity);
    }

    @Test
    public void onPauseShouldRedirectToActiveProvider() {
        dispatcherLocationProvider.setLocationProvider(defaultLocationProvider);

        dispatcherLocationProvider.onPause();

        verify(defaultLocationProvider).onPause();
        verify(continuousTask).pause();
    }

    @Test
    public void onResumeShouldRedirectToActiveProvider() {
        dispatcherLocationProvider.setLocationProvider(defaultLocationProvider);

        dispatcherLocationProvider.onResume();

        verify(defaultLocationProvider).onResume();
        verify(continuousTask).resume();
    }

    @Test
    public void onDestroyShouldRedirectToActiveProvider() {
        dispatcherLocationProvider.setLocationProvider(defaultLocationProvider);

        dispatcherLocationProvider.onDestroy();

        verify(defaultLocationProvider).onDestroy();
        verify(continuousTask).stop();
    }

    @Test
    public void cancelShouldRedirectToActiveProvider() {
        dispatcherLocationProvider.setLocationProvider(defaultLocationProvider);

        dispatcherLocationProvider.cancel();

        verify(defaultLocationProvider).cancel();
        verify(continuousTask).stop();
    }

    @Test
    public void isWaitingShouldReturnFalseWhenNoActiveProvider() {
        assertThat(dispatcherLocationProvider.isWaiting()).isFalse();
    }

    @Test
    public void isWaitingShouldRetrieveFromActiveProvider() {
        dispatcherLocationProvider.setLocationProvider(defaultLocationProvider);
        when(defaultLocationProvider.isWaiting()).thenReturn(true);

        assertThat(dispatcherLocationProvider.isWaiting()).isTrue();
        verify(defaultLocationProvider).isWaiting();
    }

    @Test
    public void isDialogShowingShouldReturnFalseWhenNoDialogShown() {
        assertThat(dispatcherLocationProvider.isDialogShowing()).isFalse();
    }

    @Test
    public void isDialogShowingShouldReturnTrueWhenGpServicesIsShowing() {
        showGpServicesDialogShown(); // so dialog is not null
        when(dialog.isShowing()).thenReturn(true);

        assertThat(dispatcherLocationProvider.isDialogShowing()).isTrue();
    }

    @Test
    public void isDialogShowingShouldRetrieveFromActiveProviderWhenExists() {
        dispatcherLocationProvider.setLocationProvider(defaultLocationProvider); // so provider is not null
        when(defaultLocationProvider.isDialogShowing()).thenReturn(true);

        assertThat(dispatcherLocationProvider.isDialogShowing()).isTrue();
        verify(defaultLocationProvider).isDialogShowing();
    }

    @Test
    public void runScheduledTaskShouldDoNothingWhenActiveProviderIsNotGPServices() {
        dispatcherLocationProvider.setLocationProvider(defaultLocationProvider);
        verify(defaultLocationProvider).configure(dispatcherLocationProvider);

        dispatcherLocationProvider.runScheduledTask(DispatcherLocationSource.GOOGLE_PLAY_SERVICE_SWITCH_TASK);

        verifyNoMoreInteractions(defaultLocationProvider);
    }

    @Test
    public void runScheduledTaskShouldDoNothingWhenNoOnGoingTask() {
        dispatcherLocationProvider.setLocationProvider(googlePlayServicesLocationProvider);
        verify(googlePlayServicesLocationProvider).configure(dispatcherLocationProvider);
        when(googlePlayServicesLocationProvider.isWaiting()).thenReturn(false);

        dispatcherLocationProvider.runScheduledTask(DispatcherLocationSource.GOOGLE_PLAY_SERVICE_SWITCH_TASK);
        verify(googlePlayServicesLocationProvider).isWaiting();

        verifyNoMoreInteractions(googlePlayServicesLocationProvider);
    }

    @Test
    public void runScheduledTaskShouldCancelCurrentProviderAndRunWithDefaultWhenGpServicesTookEnough() {
        dispatcherLocationProvider.setLocationProvider(googlePlayServicesLocationProvider);
        when(googlePlayServicesLocationProvider.isWaiting()).thenReturn(true);

        dispatcherLocationProvider.runScheduledTask(DispatcherLocationSource.GOOGLE_PLAY_SERVICE_SWITCH_TASK);

        verify(dispatcherLocationProvider).cancel();
        verify(dispatcherLocationProvider).continueWithDefaultProviders();
    }

    @Test
    public void onActivityResultShouldRedirectToActiveProvider() {
        Intent data = new Intent();
        dispatcherLocationProvider.setLocationProvider(defaultLocationProvider);
        dispatcherLocationProvider.onActivityResult(-1, -1, data);

        verify(defaultLocationProvider).onActivityResult(eq(-1), eq(-1), eq(data));
    }

    @Test
    public void onActivityResultShouldCallCheckGooglePlayServicesAvailabilityWithFalseWhenRequestCodeMatches() {
        when(dispatcherLocationSource.isGoogleApiAvailable(context)).thenReturn(RESOLVABLE_ERROR);

        dispatcherLocationProvider.onActivityResult(RequestCode.GOOGLE_PLAY_SERVICES, -1, null);

        verify(dispatcherLocationProvider).checkGooglePlayServicesAvailability(eq(false));
    }

    @Test
    public void getShouldContinueWithDefaultProviderIfThereIsNoGpServicesConfiguration() {
        when(locationConfiguration.googlePlayServicesConfiguration()).thenReturn(null);

        dispatcherLocationProvider.get();

        verify(dispatcherLocationProvider).continueWithDefaultProviders();
    }

    @Test
    public void getShouldCallCheckGooglePlayServicesAvailabilityWithTrue() {
        when(dispatcherLocationSource.isGoogleApiAvailable(context)).thenReturn(RESOLVABLE_ERROR);
        when(googlePlayServicesConfiguration.askForGooglePlayServices()).thenReturn(true);

        dispatcherLocationProvider.get();

        verify(dispatcherLocationProvider).checkGooglePlayServicesAvailability(eq(true));
    }

    @Test
    public void onFallbackShouldCallCancelAndContinueWithDefaultProviders() {
        dispatcherLocationProvider.onFallback();

        verify(dispatcherLocationProvider).cancel();
        verify(dispatcherLocationProvider).continueWithDefaultProviders();
    }

    @Test
    public void checkGooglePlayServicesAvailabilityShouldGetLocationWhenApiIsAvailable() {
        when(dispatcherLocationSource.isGoogleApiAvailable(context)).thenReturn(ConnectionResult.SUCCESS);

        dispatcherLocationProvider.checkGooglePlayServicesAvailability(false); // could be also true, wouldn't matter

        verify(dispatcherLocationProvider).getLocationFromGooglePlayServices();
    }

    @Test
    public void checkGooglePlayServicesAvailabilityShouldContinueWithDefaultWhenCalledWithFalse() {
        when(dispatcherLocationSource.isGoogleApiAvailable(context)).thenReturn(RESOLVABLE_ERROR);

        dispatcherLocationProvider.checkGooglePlayServicesAvailability(false);

        verify(dispatcherLocationProvider).continueWithDefaultProviders();
    }

    @Test
    public void checkGooglePlayServicesAvailabilityShouldAskForGooglePlayServicesWhenCalledWithTrue() {
        when(dispatcherLocationSource.isGoogleApiAvailable(context)).thenReturn(RESOLVABLE_ERROR);

        dispatcherLocationProvider.checkGooglePlayServicesAvailability(true);

        verify(dispatcherLocationProvider).askForGooglePlayServices(eq(RESOLVABLE_ERROR));
    }

    @Test
    public void askForGooglePlayServicesShouldContinueWithDefaultProvidersWhenErrorNotResolvable() {
        when(googlePlayServicesConfiguration.askForGooglePlayServices()).thenReturn(true);
        when(dispatcherLocationSource.isGoogleApiErrorUserResolvable(NOT_RESOLVABLE_ERROR)).thenReturn(false);

        dispatcherLocationProvider.askForGooglePlayServices(NOT_RESOLVABLE_ERROR);

        verify(dispatcherLocationProvider).continueWithDefaultProviders();
    }

    @Test
    public void askForGooglePlayServicesShouldContinueWithDefaultProvidersWhenConfigurationNoRequire() {
        when(googlePlayServicesConfiguration.askForGooglePlayServices()).thenReturn(false);
        when(dispatcherLocationSource.isGoogleApiErrorUserResolvable(RESOLVABLE_ERROR)).thenReturn(true);

        dispatcherLocationProvider.askForGooglePlayServices(RESOLVABLE_ERROR);

        verify(dispatcherLocationProvider).continueWithDefaultProviders();
    }

    @Test
    public void askForGooglePlayServicesShouldResolveGooglePlayServicesWhenPossible() {
        when(googlePlayServicesConfiguration.askForGooglePlayServices()).thenReturn(true);
        when(dispatcherLocationSource.isGoogleApiErrorUserResolvable(RESOLVABLE_ERROR)).thenReturn(true);

        dispatcherLocationProvider.askForGooglePlayServices(RESOLVABLE_ERROR);

        verify(dispatcherLocationProvider).resolveGooglePlayServices(RESOLVABLE_ERROR);
    }

    @Test
    public void resolveGooglePlayServicesShouldContinueWithDefaultWhenResolveDialogIsNull() {
        when(dispatcherLocationSource.getGoogleApiErrorDialog(eq(activity), eq(RESOLVABLE_ERROR),
              eq(RequestCode.GOOGLE_PLAY_SERVICES), any(OnCancelListener.class))).thenReturn(null);

        dispatcherLocationProvider.resolveGooglePlayServices(RESOLVABLE_ERROR);

        verify(dispatcherLocationProvider).continueWithDefaultProviders();
    }

    @Test
    public void resolveGooglePlayServicesShouldContinueWithDefaultWhenErrorCannotBeResolved() {

        int unresolvableError = ConnectionResult.SERVICE_INVALID;

        final DialogInterface.OnDismissListener[] dismissListener = new DialogInterface.OnDismissListener[1];

        when(dispatcherLocationSource.getGoogleApiErrorDialog(eq(activity), eq(unresolvableError),
                eq(RequestCode.GOOGLE_PLAY_SERVICES), any(OnCancelListener.class))).thenReturn(dialog);

        // catch and store real OnDismissListener listener
        doAnswer(new Answer<Object>() {
            @Override
            public Object answer(InvocationOnMock invocation) {
                dismissListener[0] = invocation.getArgument(0);

                return null;
            }
        }).when(dialog).setOnDismissListener(any(DialogInterface.OnDismissListener.class));

        // simulate dialog dismiss event
        doAnswer(new Answer<Object>() {
            @Override
            public Object answer(InvocationOnMock invocation) {
                dismissListener[0].onDismiss(dialog);

                return null;
            }
        }).when(dialog).dismiss();

        dispatcherLocationProvider.resolveGooglePlayServices(unresolvableError);

        verify(dialog).show();

        dialog.dismiss(); // Simulate dismiss dialog (error cannot be resolved)

        verify(dispatcherLocationProvider).continueWithDefaultProviders();
    }

    @Test
    public void resolveGooglePlayServicesShouldContinueWithDefaultWhenWhenResolveDialogIsCancelled() {

        final DialogInterface.OnCancelListener[] cancelListener = new OnCancelListener[1];

        // catch and store real OnCancelListener listener
        doAnswer(new Answer<Object>() {
            @Override
            public Object answer(InvocationOnMock invocation) {
                cancelListener[0] = invocation.getArgument(3);

                return dialog;
            }
        }).when(dispatcherLocationSource).getGoogleApiErrorDialog(eq(activity), eq(RESOLVABLE_ERROR),
                eq(RequestCode.GOOGLE_PLAY_SERVICES), any(OnCancelListener.class));

        // simulate dialog cancel event
        doAnswer(new Answer<Object>() {
            @Override
            public Object answer(InvocationOnMock invocation) {
                cancelListener[0].onCancel(dialog);

                return null;
            }
        }).when(dialog).cancel();

        dispatcherLocationProvider.resolveGooglePlayServices(RESOLVABLE_ERROR);

        verify(dialog).show();

        dialog.cancel(); // Simulate cancel dialog (user cancelled dialog)

        verify(dispatcherLocationProvider).continueWithDefaultProviders();
    }

    @Test
    public void resolveGooglePlayServicesShouldShowDialogWhenResolveDialogNotNull() {
        when(dispatcherLocationSource.getGoogleApiErrorDialog(eq(activity), eq(RESOLVABLE_ERROR),
              eq(RequestCode.GOOGLE_PLAY_SERVICES), any(OnCancelListener.class))).thenReturn(dialog);

        dispatcherLocationProvider.resolveGooglePlayServices(RESOLVABLE_ERROR);

        verify(dialog).show();
    }

    @Test
    public void getLocationFromGooglePlayServices() {
        dispatcherLocationProvider.getLocationFromGooglePlayServices();

        verify(googlePlayServicesLocationProvider).configure(dispatcherLocationProvider);
        verify(continuousTask).delayed(GOOGLE_PLAY_SERVICES_SWITCH_PERIOD);
        verify(googlePlayServicesLocationProvider).get();
    }

    @Test
    public void continueWithDefaultProvidersShouldNotifyFailWhenNoDefaultProviderConfiguration() {
        when(locationConfiguration.defaultProviderConfiguration()).thenReturn(null);

        dispatcherLocationProvider.continueWithDefaultProviders();

        //noinspection WrongConstant
        verify(locationListener).onLocationFailed(eq(FailType.GOOGLE_PLAY_SERVICES_NOT_AVAILABLE));
    }

    @Test
    public void continueWithDefaultProviders() {
        dispatcherLocationProvider.continueWithDefaultProviders();

        verify(defaultLocationProvider).configure(dispatcherLocationProvider);
        verify(defaultLocationProvider).get();
    }

    @Test
    public void setLocationProviderShouldConfigureGivenProvider() {
        dispatcherLocationProvider.setLocationProvider(defaultLocationProvider);
        verify(defaultLocationProvider).configure(dispatcherLocationProvider);
        dispatcherLocationProvider.setLocationProvider(googlePlayServicesLocationProvider);
        verify(googlePlayServicesLocationProvider).configure(dispatcherLocationProvider);
    }

    private void showGpServicesDialogShown() {
        when(googlePlayServicesConfiguration.askForGooglePlayServices()).thenReturn(true);
        when(dispatcherLocationSource.isGoogleApiAvailable(context)).thenReturn(RESOLVABLE_ERROR);
        when(dispatcherLocationSource.isGoogleApiErrorUserResolvable(RESOLVABLE_ERROR)).thenReturn(true);
        when(dispatcherLocationSource.getGoogleApiErrorDialog(eq(activity), eq(RESOLVABLE_ERROR),
              eq(RequestCode.GOOGLE_PLAY_SERVICES), any(OnCancelListener.class))).thenReturn(dialog);

        dispatcherLocationProvider.checkGooglePlayServicesAvailability(true);

        verify(dialog).show();
    }


}