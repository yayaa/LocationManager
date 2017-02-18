package com.yayandroid.locationmanager;

import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.support.annotation.NonNull;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.yayandroid.locationmanager.configuration.LocationConfiguration;
import com.yayandroid.locationmanager.constants.FailType;
import com.yayandroid.locationmanager.constants.LogType;
import com.yayandroid.locationmanager.constants.ProviderType;
import com.yayandroid.locationmanager.constants.RequestCode;
import com.yayandroid.locationmanager.helper.LocationUtils;
import com.yayandroid.locationmanager.helper.LogUtils;
import com.yayandroid.locationmanager.helper.continuoustask.ContinuousTask;
import com.yayandroid.locationmanager.helper.continuoustask.ContinuousTask.ContinuousTaskRunner;
import com.yayandroid.locationmanager.listener.LocationListener;
import com.yayandroid.locationmanager.listener.PermissionListener;
import com.yayandroid.locationmanager.providers.locationprovider.DefaultLocationProvider;
import com.yayandroid.locationmanager.providers.locationprovider.GPServicesLocationProvider;
import com.yayandroid.locationmanager.providers.locationprovider.LocationProvider;
import com.yayandroid.locationmanager.providers.permissionprovider.DefaultPermissionProvider;
import com.yayandroid.locationmanager.providers.permissionprovider.PermissionProvider;
import com.yayandroid.locationmanager.view.ContextProcessor;

public class LocationManager implements ContinuousTaskRunner, PermissionListener {

    private static final String GOOGLE_PLAY_SERVICE_SWITCH_TASK = "googlePlayServiceSwitchTask";
    private final ContinuousTask gpServicesSwitchTask = new ContinuousTask(GOOGLE_PLAY_SERVICE_SWITCH_TASK, this);

    private int locationFrom = ProviderType.NONE;

    private ContextProcessor contextProcessor;
    private Dialog gpServicesDialog;
    private LocationListener listener;
    private LocationConfiguration configuration;
    private LocationProvider activeProvider;
    private PermissionProvider permissionProvider;

    /**
     * This library contains a lot of log to make tracing steps easier,
     * So you can set the type which one corresponds your requirements.
     * Do not forget to set it to NONE before you publish your application!
     */
    public static void setLogType(@LogType.Level int type) {
        LogUtils.setLogType(type);
    }

    /**
     * To create an instance of this manager you MUST specify a LocationConfiguration
     */
    public LocationManager(LocationConfiguration configuration) {
        this.configuration = configuration;
    }

    /**
     * This specifies on which activity this manager will runScheduledTask,
     * this also needs to be set before you attempt to get location
     */
    public LocationManager on(Context context) {
        this.contextProcessor = new ContextProcessor(context);
        return this;
    }

    /**
     * Specify a LocationListener to receive location when it is available,
     * or get knowledge of any other steps in process
     */
    public LocationManager notify(LocationListener listener) {
        this.listener = listener;
        return this;
    }

    /**
     * Instead of using {@linkplain DefaultLocationProvider} you can create your own,
     * and set it to manager so it will run your LocationProvider.
     */
    public LocationManager setLocationProvider(LocationProvider provider) {
        if (provider != null) {
            provider.configure(contextProcessor, configuration);
        }

        this.activeProvider = provider;
        return this;
    }

    /**
     * Instead of using {@linkplain DefaultPermissionProvider} you can implement your own,
     * and set it to manager it will use your PermissionProvider.
     */
    public LocationManager setPermissionProvider(PermissionProvider permissionProvider) {
        this.permissionProvider = permissionProvider;
        return this;
    }

    /**
     * Possible to change configuration object on runtime
     */
    public LocationManager setConfiguration(LocationConfiguration configuration) {
        this.configuration = configuration;
        return this;
    }

    /**
     * Returns configuration object which is defined to this manager
     */
    public LocationConfiguration getConfiguration() {
        return configuration;
    }

    /**
     * Google suggests to stop location updates when the activity is no longer in focus
     * http://developer.android.com/training/location/receive-location-updates.html#stop-updates
     */
    public void onPause() {
        if (activeProvider != null) {
            activeProvider.onPause();
        }
        gpServicesSwitchTask.pause();
    }

    /**
     * Restart location updates to keep continue getting locations when activity is back
     */
    public void onResume() {
        if (activeProvider != null) {
            activeProvider.onResume();
        }
        gpServicesSwitchTask.resume();
    }

    /**
     * Release whatever you need to when activity is destroyed
     */
    public void onDestroy() {
        if (activeProvider != null) {
            activeProvider.onDestroy();
        }

        gpServicesSwitchTask.stop();
        gpServicesDialog = null;
        listener = null;
        contextProcessor = null;
        activeProvider = null;
        configuration = null;
    }

    /**
     * This is required to check when user handles with Google Play Services error, or enables GPS...
     */
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == RequestCode.GOOGLE_PLAY_SERVICES) {
            // Check whether do we have gpServices now or still not!
            get(false);
        } else {
            if (activeProvider != null && activeProvider.requiresActivityResult()) {
                activeProvider.onActivityResult(requestCode, resultCode, data);
            }
        }
    }

    /**
     * Provide requestPermissionResult to manager so the it can handle RuntimePermission
     */
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        getPermissionProvider().onRequestPermissionsResult(requestCode, permissions, grantResults);
    }

    /**
     * To determine whether LocationManager is currently waiting for location or it did already receive one!
     */
    public boolean isWaitingForLocation() {
        if (activeProvider != null) {
            return activeProvider.isWaiting();
        } else {
            return false;
        }
    }

    /**
     * To determine whether the manager is currently displaying any dialog or not
     */
    public boolean isAnyDialogShowing() {
        boolean providersDisplaying = (activeProvider != null && activeProvider.isDialogShowing());
        boolean gpServicesDisplaying = (gpServicesDialog != null && gpServicesDialog.isShowing());
        return gpServicesDisplaying || providersDisplaying;
    }

    /**
     * The only method you need to call to trigger getting location process
     */
    public void get() {
        get(true);
    }

    /**
     * Abort the mission and cancel all location update requests
     */
    public void cancel() {
        if (activeProvider != null) {
            activeProvider.cancel();
        }
    }

    private void get(boolean askForGPServices) {
        if (contextProcessor == null)
            throw new RuntimeException("You must set a context to runScheduledTask LocationManager on!");

        if (configuration.gpServicesConfiguration() == null) {
            LogUtils.logI("Configuration requires not to use Google Play Services, " +
                    "so skipping that step to Default Location Providers", LogType.GENERAL);
            continueWithDefaultProviders();
            return;
        }

        int gpServicesAvailability = LocationUtils.isGooglePlayServicesAvailable(contextProcessor.getContext());

        if (gpServicesAvailability == ConnectionResult.SUCCESS) {
            LogUtils.logI("GooglePlayServices is available on device.", LogType.GENERAL);
            askForPermission(ProviderType.GOOGLE_PLAY_SERVICES);
        } else {
            LogUtils.logI("GooglePlayServices is NOT available on device.", LogType.IMPORTANT);

            if (askForGPServices) {
                if (configuration.gpServicesConfiguration().askForGPServices() &&
                        GoogleApiAvailability.getInstance()
                                .isUserResolvableError(gpServicesAvailability)) {

                    LogUtils.logI("Asking user to handle GooglePlayServices error...", LogType.GENERAL);
                    gpServicesDialog = LocationUtils.getGooglePlayServicesErrorDialog(contextProcessor.getContext(),
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
                              + "is not running on an Activity, dialog cannot be displayed.", LogType.GENERAL);
                        continueWithDefaultProviders();
                    }
                } else {
                    LogUtils.logI("Either GooglePlayServices error is not resolvable "
                            + "or the configuration doesn't wants us to bother user.", LogType.GENERAL);
                    continueWithDefaultProviders();
                }

            } else {
                LogUtils.logI("GooglePlayServices is NOT available and "
                        + "even though we ask user to handle error, "
                        + "it is still NOT available.", LogType.IMPORTANT);

                // This means get method is called by onActivityResult
                // which we already ask user to handle with gpServices error
                continueWithDefaultProviders();
            }
        }
    }

    private void continueWithDefaultProviders() {
        if (configuration.defaultProviderConfiguration() == null) {
            LogUtils.logI("Because the configuration, we can only use GooglePlayServices, so we abort.", LogType.GENERAL);
            failed(FailType.GP_SERVICES_NOT_AVAILABLE);
        } else {
            LogUtils.logI("Attempting to get location from default providers...", LogType.GENERAL);
            askForPermission(ProviderType.DEFAULT_PROVIDERS);
        }
    }

    private void askForPermission(@ProviderType.Source int locationFrom) {
        this.locationFrom = locationFrom;
        if (getPermissionProvider().hasPermission()) {
            locationPermissionGranted(true);
        } else {
            if (getPermissionProvider().requestPermissions()) {
                LogUtils.logI("Waiting until we receive any callback from PermissionProvider...", LogType.GENERAL);
            } else {
                LogUtils.logI("We don't have permissions and cannot ask for it. Aborting...", LogType.GENERAL);
                failed(FailType.PERMISSION_DENIED);
            }
        }
    }

    private void locationPermissionGranted(boolean alreadyHadPermission) {
        LogUtils.logI("We got permission, getting location...", LogType.GENERAL);

        if (listener != null) {
            listener.onPermissionGranted(alreadyHadPermission);
        }

        if (locationFrom == ProviderType.GOOGLE_PLAY_SERVICES) {
            setLocationProvider(new GPServicesLocationProvider());
            gpServicesSwitchTask.delayed(configuration.gpServicesConfiguration().gpServicesWaitPeriod());
        } else {
            // To ensure not to use same provider again!
            setLocationProvider(null);
        }

        getLocation();
    }

    private void getLocation() {
        getActiveProvider().notifyTo(listener);
        getActiveProvider().get();
    }

    private LocationProvider getActiveProvider() {
        if (activeProvider == null) {
            setLocationProvider(new DefaultLocationProvider());
        }
        return activeProvider;
    }

    private PermissionProvider getPermissionProvider() {
        if (permissionProvider == null) {
            permissionProvider = new DefaultPermissionProvider(contextProcessor, this,
                  getConfiguration().requiredPermissions(), getConfiguration().rationalMessage());
        }
        return permissionProvider;
    }

    private void failed(int type) {
        if (listener != null) {
            listener.onLocationFailed(type);
        }
    }

    @Override
    public void runScheduledTask(@NonNull String taskId) {
        if (taskId.equals(GOOGLE_PLAY_SERVICE_SWITCH_TASK)) {
            if (activeProvider instanceof GPServicesLocationProvider && activeProvider.isWaiting()) {
                LogUtils.logI("We couldn't receive location from GooglePlayServices, "
                      + "so switching default providers...", LogType.IMPORTANT);
                cancel();
                continueWithDefaultProviders();
            }
        }
    }

    @Override
    public void onPermissionsGranted() {
        locationPermissionGranted(false);
    }

    @Override
    public void onPermissionsDenied() {
        failed(FailType.PERMISSION_DENIED);
    }
}