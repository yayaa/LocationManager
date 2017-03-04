package com.yayandroid.locationmanager.providers.locationprovider;

import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.support.annotation.NonNull;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.yayandroid.locationmanager.constants.FailType;
import com.yayandroid.locationmanager.constants.RequestCode;
import com.yayandroid.locationmanager.helper.LocationUtils;
import com.yayandroid.locationmanager.helper.LogUtils;
import com.yayandroid.locationmanager.helper.continuoustask.ContinuousTask;
import com.yayandroid.locationmanager.helper.continuoustask.ContinuousTask.ContinuousTaskRunner;

public class DispatcherLocationProvider extends LocationProvider implements ContinuousTaskRunner {

    private static final String GOOGLE_PLAY_SERVICE_SWITCH_TASK = "googlePlayServiceSwitchTask";
    private final ContinuousTask gpServicesSwitchTask = new ContinuousTask(GOOGLE_PLAY_SERVICE_SWITCH_TASK, this);

    private LocationProvider activeProvider;
    private Dialog gpServicesDialog;

    @Override
    public void onPause() {
        super.onPause();

        if (activeProvider != null) {
            activeProvider.onPause();
        }

        gpServicesSwitchTask.pause();
    }

    @Override
    public void onResume() {
        super.onResume();

        if (activeProvider != null) {
            activeProvider.onResume();
        }

        gpServicesSwitchTask.resume();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        if (activeProvider != null) {
            activeProvider.onDestroy();
        }

        gpServicesSwitchTask.stop();
        gpServicesDialog = null;
    }

    @Override
    public boolean isWaiting() {
        return activeProvider != null && activeProvider.isWaiting();
    }

    @Override
    public boolean isDialogShowing() {
        boolean gpServicesDialogShown = gpServicesDialog != null && gpServicesDialog.isShowing();
        boolean anyProviderDialogShown = activeProvider != null && activeProvider.isDialogShowing();
        return gpServicesDialogShown || anyProviderDialogShown;
    }

    @Override
    public void cancel() {
        if (activeProvider != null) {
            activeProvider.cancel();
        }
    }

    @Override
    public void runScheduledTask(@NonNull String taskId) {
        if (taskId.equals(GOOGLE_PLAY_SERVICE_SWITCH_TASK)) {
            if (activeProvider instanceof GPServicesLocationProvider && activeProvider.isWaiting()) {
                LogUtils.logI("We couldn't receive location from GooglePlayServices, so switching default providers...");
                cancel();
                continueWithDefaultProviders();
            }
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == RequestCode.GOOGLE_PLAY_SERVICES) {
            // Check whether do we have gpServices now or still not!
            checkGooglePlayServicesAvailability(false);
        } else {
            if (activeProvider != null) {
                activeProvider.onActivityResult(requestCode, resultCode, data);
            }
        }
    }

    @Override
    public void get() {
        if (getConfiguration().gpServicesConfiguration() != null) {
            checkGooglePlayServicesAvailability(true);
        } else {
            LogUtils.logI("Configuration requires not to use Google Play Services, "
                  + "so skipping that step to Default Location Providers");
            continueWithDefaultProviders();
        }
    }

    private void checkGooglePlayServicesAvailability(boolean askForGPServices) {
        int gpServicesAvailability = LocationUtils.isGooglePlayServicesAvailable(getContext());

        if (gpServicesAvailability == ConnectionResult.SUCCESS) {
            LogUtils.logI("GooglePlayServices is available on device.");
            getLocationFromGooglePlayServices();
        } else {
            LogUtils.logI("GooglePlayServices is NOT available on device.");
            if (askForGPServices) {
                if (getConfiguration().gpServicesConfiguration().askForGooglePlayServices() &&
                      GoogleApiAvailability.getInstance().isUserResolvableError(gpServicesAvailability)) {

                    LogUtils.logI("Asking user to handle GooglePlayServices error...");
                    gpServicesDialog = LocationUtils.getGooglePlayServicesErrorDialog(getContext(),
                          gpServicesAvailability, RequestCode.GOOGLE_PLAY_SERVICES, new DialogInterface.OnCancelListener() {
                              @Override
                              public void onCancel(DialogInterface dialog) {
                                  failed(FailType.GP_SERVICES_NOT_AVAILABLE);
                              }
                          });

                    if (gpServicesDialog != null) {
                        gpServicesDialog.show();
                    } else {
                        LogUtils.logI("GooglePlayServices error could've been resolved, but since LocationManager "
                              + "is not running on an Activity, dialog cannot be displayed.");
                        continueWithDefaultProviders();
                    }
                } else {
                    LogUtils.logI("Either GooglePlayServices error is not resolvable "
                          + "or the configuration doesn't wants us to bother user.");
                    continueWithDefaultProviders();
                }
            } else {
                LogUtils.logI("GooglePlayServices is NOT available and even though we ask user to handle error, "
                      + "it is still NOT available.");

                // This means get method is called by onActivityResult
                // which we already ask user to handle with gpServices error
                continueWithDefaultProviders();
            }
        }
    }

    private void getLocationFromGooglePlayServices() {
        LogUtils.logI("Attempting to get location from Google Play Services providers...");
        setLocationProvider(new GPServicesLocationProvider());
        gpServicesSwitchTask.delayed(getConfiguration().gpServicesConfiguration().googlePlayServicesWaitPeriod());
        activeProvider.get();
    }

    private void continueWithDefaultProviders() {
        LogUtils.logI("Attempting to get location from default providers...");
        setLocationProvider(new DefaultLocationProvider());
        activeProvider.get();
    }

    public void setLocationProvider(LocationProvider provider) {
        this.activeProvider = provider;
        activeProvider.configure(this);
    }

    private void failed(@FailType.Reason int type) {
        if (getListener() != null) {
            getListener().onLocationFailed(type);
        }
    }
}
