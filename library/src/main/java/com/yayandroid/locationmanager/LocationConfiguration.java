package com.yayandroid.locationmanager;

import com.google.android.gms.location.LocationRequest;
import com.yayandroid.locationmanager.constants.Default;
import com.yayandroid.locationmanager.constants.ProviderType;

/**
 * Created by Yahya Bayramoglu on 09/02/16.
 */
public class LocationConfiguration {

    private LocationRequest locationRequest;

    private boolean keepTracking = false;
    private boolean askForEnableGPS = true;
    private boolean askForGPServices = false;
    private boolean askForSettingsApi = true;
    private boolean doNotUseGPServices = false;
    private boolean useOnlyGPServices = false;
    private boolean failOnConnectionSuspended = true;
    private boolean failOnSettingsApiSuspended = false;

    private String rationalMessage = "";
    private String gpsMessage = "";

    private String[] requiredPermissions = Default.LOCATION_PERMISSIONS;

    private float acceptableAccuracy = Default.MIN_ACCURACY;
    private long acceptableTimePeriod = Default.TIME_PERIOD;
    private long requiredTimeInterval = Default.LOCATION_INTERVAL;
    private long gpsWaitPeriod = Default.WAIT_PERIOD;
    private long networkWaitPeriod = Default.WAIT_PERIOD;
    private long gpServicesWaitPeriod = Default.WAIT_PERIOD;

    /**
     * If you need to keep receiving location updates, then you need to set this as true.
     * Otherwise manager will be aborted after any location received.
     * <p>
     * Default is False.
     */
    public LocationConfiguration keepTracking(boolean track) {
        this.keepTracking = track;
        return this;
    }

    /**
     * While trying to get location from GPS Provider,
     * manager will check whether it is available or not.
     * Then if this flag is on it will ask user to turn it on,
     * if not it will switch directly to Network Provider.
     * <p>
     * Default is True.
     */
    public LocationConfiguration askForEnableGPS(boolean askFor) {
        this.askForEnableGPS = askFor;
        return this;
    }

    /**
     * Set true to ask user handle when there is some resolvable error
     * on connection GooglePlayServices, if you don't want to bother user
     * to configure Google Play Services to receive location then set this as false.
     * <p>
     * Default is False.
     */
    public LocationConfiguration askForGooglePlayServices(boolean askForGPServices) {
        this.askForGPServices = askForGPServices;
        return this;
    }

    /**
     * While trying to get location via GooglePlayServices LocationApi,
     * manager will check whether GPS, Wifi and Cell networks are available or not.
     * Then if this flag is on it will ask user to turn them on, again, via GooglePlayServices
     * by displaying a system dialog if not it will directly try to receive location
     * -which probably not going to return no values.
     * <p>
     * Default is True.
     */
    public LocationConfiguration askForSettingsApi(boolean askForSettingsApi) {
        this.askForSettingsApi = askForSettingsApi;
        return this;
    }

    /**
     * While this is set true default location providers (GPS & Network)
     * will not be used to receive location, set false if you want them to be used.
     * <p>
     * Default is False.
     */
    public LocationConfiguration useOnlyGPServices(boolean onlyGPServices) {
        this.useOnlyGPServices = onlyGPServices;
        return this;
    }

    /**
     * If you set this true, it will cause not to use Google Play Services at all
     * and manager will directly get location from
     * default location providers which are GPS or Network.
     * <p>
     * Default is False.
     */
    public LocationConfiguration doNotUseGooglePlayServices(boolean use) {
        this.doNotUseGPServices = use;
        return this;
    }

    /**
     * As it is described in official documentation when Google Play Services is disconnected,
     * it will call ConnectionSuspended and after some time it will try to reconnect
     * you can determine to fail in this situation or you may want to wait.
     * <p>
     * Default is True.
     * <p>
     * https://developers.google.com/android/reference/com/google/android/gms/common/api/GoogleApiClient.ConnectionCallbacks#onConnectionSuspended(int)
     */
    public LocationConfiguration failOnConnectionSuspended(boolean shouldFail) {
        this.failOnConnectionSuspended = shouldFail;
        return this;
    }

    /**
     * This flag will be checked when it is not possible to display user a settingsApi dialog
     * to switch necessary providers on, or when there is an error displaying the dialog.
     * If the flag is on, then manager will notify listener as location failed,
     * otherwise it will try to get location anyway -which probably not gonna happen.
     * <p>
     * Default is False. -Because after GooglePlayServices Provider it might switch
     * to default providers, if we fail here then those provider will never trigger.
     */
    public LocationConfiguration failOnSettingsApiSuspended(boolean shouldFail) {
        this.failOnSettingsApiSuspended = shouldFail;
        return this;
    }

    /**
     * If you need to ask any other permissions beside {@linkplain Default#LOCATION_PERMISSIONS}
     * or you may not need both of those permissions, you can change permissions
     * by calling this method with new permissions' array.
     */
    public LocationConfiguration setPermissions(String[] permissions) {
        this.requiredPermissions = permissions;
        return this;
    }

    /**
     * Indicates what to display when user needs to see a rational dialog for RuntimePermission.
     * There is no default value, so if you do not set this it will create an empty dialog.
     */
    public LocationConfiguration setRationalMessage(String message) {
        this.rationalMessage = message;
        return this;
    }

    /**
     * Indicates what to display to user while asking to turn GPS on.
     * There is no default value, so if you do not set this it will create an empty dialog.
     */
    public LocationConfiguration setGPSMessage(String message) {
        this.gpsMessage = message;
        return this;
    }

    /**
     * LocationRequest object that you specified to use while getting location from Google Play Services
     * <p>
     * Default is {@linkplain LocationConfiguration#generateDefaultLocationRequest()}
     */
    public LocationConfiguration setLocationRequest(LocationRequest request) {
        this.locationRequest = request;
        return this;
    }

    /**
     * Minimum Accuracy that you seek location to be
     * <p>
     * Default is {@linkplain Default#MIN_ACCURACY}
     */
    public LocationConfiguration setMinAccuracy(float minAccuracy) {
        this.acceptableAccuracy = minAccuracy;
        return this;
    }

    /**
     * Indicates time period that can be count as usable location,
     * this needs to be considered such as "last 5 minutes"
     * <p>
     * Default is {@linkplain Default#TIME_PERIOD}
     */
    public LocationConfiguration setWithinTimePeriod(long milliseconds) {
        this.acceptableTimePeriod = milliseconds;
        return this;
    }

    /**
     * TimeInterval will be used while getting location from default location providers
     * It will define in which period updates need to be delivered
     * <p>
     * Default is {@linkplain Default#LOCATION_INTERVAL}
     */
    public LocationConfiguration setTimeInterval(long milliseconds) {
        this.requiredTimeInterval = milliseconds;
        return this;
    }

    /**
     * Indicates waiting time period before switching to next possible provider.
     * Possible to set provider wait periods separately by passing providerType as one of the
     * {@linkplain ProviderType.Source} values.
     * <p>
     * Default values are {@linkplain Default#WAIT_PERIOD}
     */
    public LocationConfiguration setWaitPeriod(@ProviderType.Source int providerType, long milliseconds) {
        switch (providerType) {
            case ProviderType.GOOGLE_PLAY_SERVICES: {
                this.gpServicesWaitPeriod = milliseconds;
                break;
            }
            case ProviderType.NETWORK: {
                this.networkWaitPeriod = milliseconds;
                break;
            }
            case ProviderType.GPS: {
                this.gpsWaitPeriod = milliseconds;
                break;
            }
            case ProviderType.DEFAULT_PROVIDERS: {
                this.gpsWaitPeriod = milliseconds;
                this.networkWaitPeriod = milliseconds;
                break;
            }
        }
        return this;
    }

    public boolean shouldAskForGPServices() {
        return askForGPServices;
    }

    public boolean shouldAskForSettingsApi() {
        return askForSettingsApi;
    }

    public boolean shouldNotUseGPServices() {
        return doNotUseGPServices;
    }

    public boolean shouldUseOnlyGPServices() {
        return useOnlyGPServices;
    }

    public boolean shouldKeepTracking() {
        return keepTracking;
    }

    public boolean shouldFailWhenConnectionSuspended() {
        return failOnConnectionSuspended;
    }

    public boolean shouldFailWhenSettingsApiSuspended() {
        return failOnSettingsApiSuspended;
    }

    public boolean shouldAskForEnableGPS() {
        return askForEnableGPS;
    }

    public String[] getRequiredPermissions() {
        return requiredPermissions;
    }

    public String getRationalMessage() {
        return rationalMessage;
    }

    public String getGPSMessage() {
        return gpsMessage;
    }

    public LocationRequest getLocationRequest() {
        if (locationRequest == null) {
            generateDefaultLocationRequest();
        }
        return locationRequest;
    }

    public float getAcceptableAccuracy() {
        return acceptableAccuracy;
    }

    public long getAcceptableTimePeriod() {
        return acceptableTimePeriod;
    }

    public long getRequiredTimeInterval() {
        return requiredTimeInterval;
    }

    public long getGPSWaitPeriod() {
        return gpsWaitPeriod;
    }

    public long getNetworkWaitPeriod() {
        return networkWaitPeriod;
    }

    public long getGPServicesWaitPeriod() {
        return gpServicesWaitPeriod;
    }

    /**
     * https://developers.google.com/android/reference/com/google/android/gms/location/LocationRequest
     */
    private void generateDefaultLocationRequest() {
        this.locationRequest = LocationRequest.create();
        locationRequest.setPriority(Default.LOCATION_PRIORITY);
        locationRequest.setInterval(Default.LOCATION_INTERVAL);
        locationRequest.setFastestInterval(Default.LOCATION_FASTEST_INTERVAL);
    }

}