package com.yayandroid.locationmanager.providers.locationprovider;

import android.app.Dialog;
import android.content.Intent;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.provider.Settings;

import com.yayandroid.locationmanager.constants.FailType;
import com.yayandroid.locationmanager.constants.ProcessType;
import com.yayandroid.locationmanager.constants.ProviderType;
import com.yayandroid.locationmanager.constants.RequestCode;
import com.yayandroid.locationmanager.helper.LogUtils;
import com.yayandroid.locationmanager.listener.DialogListener;
import com.yayandroid.locationmanager.providers.dialogprovider.DialogProvider;
import com.yayandroid.locationmanager.providers.locationprovider.AlternativeDefaultLocationSource.LocationUpdateListener;


public class AlternativeDefaultLocationProvider extends LocationProvider implements DialogListener, LocationUpdateListener {

    private boolean isRunning;

    private AlternativeDefaultLocationSource gpsProvider;
    private AlternativeDefaultLocationSource networkProvider;

    private Location lastLocation;
    private long lastTime;

    private Dialog gpsDialog;
    private boolean isGPSDialogDismissed;

    public AlternativeDefaultLocationProvider(){
        isGPSDialogDismissed = false;
        isRunning = false;
    }

    @Override
    public void initialize() {
        super.initialize();

        gpsProvider = new AlternativeDefaultLocationSource(getContext(), ProviderType.GPS);
        networkProvider = new AlternativeDefaultLocationSource(getContext(), ProviderType.NETWORK);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        gpsDialog = null;
    }

    @Override
    public void onPause() {
        super.onPause();

        dismissGpsDialog();

        stopLocationListener();
    }

    @Override
    public void onResume() {
        super.onResume();

        gpsDialogDismissedAction();

        if(!isDialogShowing() && isAnyProviderEnable()){
            askForLocation();
        }
    }

    @Override
    public void cancel() {
        stopLocationListener();
    }

    /* if gps dialog is dismissed and gps provider is available(user activated by going settings manually) continue
      or if provider is not available, show dialog again
    */
    private void gpsDialogDismissedAction(){
        if(isGPSDialogDismissed){
            isGPSDialogDismissed = false;
            if(isGPSProviderEnable()){
                askForLocation();
            } else {
                askForEnableGPS();
            }
        }
    }

    private void dismissGpsDialog(){
        if(isDialogShowing()){
            gpsDialog.dismiss();
            isGPSDialogDismissed = true;
        }
    }

    @Override
    public boolean isDialogShowing() {
        return gpsDialog != null && gpsDialog.isShowing();
    }

    @Override
    public void get() {
        if(isGPSProviderEnable()){
            askForLocation();
            LogUtils.logI("GPS is already enabled, getting location...");
        } else {
            // GPS is not enabled,
            if (getConfiguration().defaultProviderConfiguration().askForEnableGPS() && getActivity() != null) {
                LogUtils.logI("GPS is not enabled");
                askForEnableGPS();
            } else {
                LogUtils.logI("There is no GPS dialog provider, moving on with Network...");
                checkNetworkProviderAction();
            }
        }
    }

    private void askForLocation() {
        boolean locationIsAlreadyAvailable = checkForLastKnowLocation();

        if (getConfiguration().keepTracking() || !locationIsAlreadyAvailable) {
            LogUtils.logI("Ask for location update...");
            startLocationListener();
        } else {
            LogUtils.logI("We got location, no need to ask for location updates.");
        }
    }

    private boolean checkForLastKnowLocation() {

        if(gpsProvider.hasSufficientLastKnownLocation(gpsProvider.getLastKnownLocation(),
               getConfiguration().defaultProviderConfiguration().acceptableTimePeriod(),
               getConfiguration().defaultProviderConfiguration().acceptableAccuracy())){

            LogUtils.logI("LastKnowLocation is usable.");
            notifyProcessChange(LocationManager.GPS_PROVIDER);
            onLocationReceived(gpsProvider.getLastKnownLocation());

            return true;

        } else if(networkProvider.hasSufficientLastKnownLocation(networkProvider.getLastKnownLocation(),
                getConfiguration().defaultProviderConfiguration().acceptableTimePeriod(),
                getConfiguration().defaultProviderConfiguration().acceptableAccuracy())) {

            LogUtils.logI("LastKnowLocation is usable.");
            notifyProcessChange(LocationManager.NETWORK_PROVIDER);
            onLocationReceived(networkProvider.getLastKnownLocation());

            return true;
        } else {

            LogUtils.logI("LastKnowLocation is not usable.");
            return false;
        }
    }

    private void startLocationListener() {

        if(isRunning){
            return;
        }

        LogUtils.logI("Starting both providers...");
        gpsProvider.startLocationListener(this, getConfiguration().defaultProviderConfiguration().requiredTimeInterval(),
                getConfiguration().defaultProviderConfiguration().requiredDistanceInterval());
        networkProvider.startLocationListener(this, getConfiguration().defaultProviderConfiguration().requiredTimeInterval(),
                getConfiguration().defaultProviderConfiguration().requiredDistanceInterval());

        isRunning = true;
    }

    private void stopLocationListener() {
        if(isRunning){
            LogUtils.logI("Stopping both providers...");
            gpsProvider.stopLocationListener();
            networkProvider.stopLocationListener();
            isRunning = false;
        }
    }

    private void askForEnableGPS() {
        LogUtils.logI("Asking user to enable GPS");
        DialogProvider gpsDialogProvider = getConfiguration().defaultProviderConfiguration().gpsDialogProvider();
        gpsDialogProvider.setDialogListener(this);
        gpsDialog = gpsDialogProvider.getDialog(getActivity());
        gpsDialog.show();
    }

    private void checkNetworkProviderAction(){
        if(isNetworkProviderEnable()){
            askForLocation();
        } else {
            LogUtils.logI("Network Provider is not available, calling fail...");
            onLocationFailed(FailType.NETWORK_NOT_AVAILABLE);
            stopLocationListener();
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == RequestCode.GPS_ENABLE) {
            if (isGPSProviderEnable()) {
                askForLocation();
            } else {
                LogUtils.logI("User didn't activate GPS, so continue with Network Provider");
                checkNetworkProviderAction();
            }
        }
    }

    private boolean isNewLocationBetter(Location newLocation, long newTime){

        boolean update = false;
        boolean isMoreAccurate = false;
        int accuracyDelta;

        // Check whether the new location fix is more or less accurate
        if(lastLocation != null){
            accuracyDelta = (int) (newLocation.getAccuracy() - lastLocation.getAccuracy());
            isMoreAccurate = accuracyDelta < 0;
        }

        // Updates only if there is no last location, the provider is the same, or the provider is more accurate, or the old location is stale
        if(lastLocation == null){
            update = true;
        }
        else if(isMoreAccurate){
            update = true;
        }
        else if(lastLocation.getProvider().equals(newLocation.getProvider())){
            update = true;
        }
        else if(newLocation.getProvider().equals(LocationManager.GPS_PROVIDER)){
            update = true;
        }
        else if (newTime - lastTime > getConfiguration().defaultProviderConfiguration().acceptableTimePeriod()){
            update = true;
        }

        return update;
    }

    @Override
    public void onUpdateLocation(Location oldLocation, long oldTime, Location newLocation, long newTime) {

        // Updates the location and notices about this change
        if(isNewLocationBetter(newLocation, newTime)){
            LogUtils.logI("Better location came up, updating the old one.");
            notifyProcessChange(newLocation.getProvider());
            onLocationReceived(newLocation);

            lastLocation = newLocation;
            lastTime = newTime;

            if (!getConfiguration().keepTracking()) {
                stopLocationListener();
            }
        }
    }

    private void notifyProcessChange(String provider) {
        if (getListener() != null) {
            getListener().onProcessTypeChanged(LocationManager.GPS_PROVIDER.equals(provider)
                    ? ProcessType.GETTING_LOCATION_FROM_GPS_PROVIDER
                    : ProcessType.GETTING_LOCATION_FROM_NETWORK_PROVIDER);
        }
    }

    private boolean isGPSProviderEnable(){
        return gpsProvider.isProviderEnabled();
    }

    private boolean isNetworkProviderEnable(){
        return networkProvider.isProviderEnabled();
    }

    private boolean isAnyProviderEnable(){
        return isNetworkProviderEnable() || isGPSProviderEnable();
    }

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {
        if (getListener() != null) {
            getListener().onStatusChanged(provider, status, extras);
        }
    }

    @Override
    public void onProviderEnabled(String provider) {
        if (getListener() != null) {
            getListener().onProviderEnabled(provider);
        }
    }

    @Override
    public void onProviderDisabled(String provider) {
        if (getListener() != null) {
            getListener().onProviderDisabled(provider);
        }
    }

    private void onLocationFailed(@FailType int type) {
        if (getListener() != null) {
            getListener().onLocationFailed(type);
        }
    }

    private void onLocationReceived(Location location) {
        if (getListener() != null) {
            getListener().onLocationChanged(location);
        }
    }

    @Override
    public void onPositiveButtonClick() {
        if (isGPSProviderEnable()) {
            LogUtils.logI("User didn't activate GPS through the dialog, but activated manually");
            askForLocation();
        } else {
            boolean activityStarted = startActivityForResult(new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS),
                    RequestCode.GPS_ENABLE);
            if (!activityStarted) {
                onLocationFailed(FailType.VIEW_NOT_REQUIRED_TYPE);
            }
        }
    }

    @Override
    public void onNegativeButtonClick() {
        if (isGPSProviderEnable()) {
            LogUtils.logI("User didn't activate GPS through the dialog, but activated manually");
            askForLocation();
        } else {
            LogUtils.logI("User didn't want to enable GPS, checking Network");
            checkNetworkProviderAction();
        }
    }
}
