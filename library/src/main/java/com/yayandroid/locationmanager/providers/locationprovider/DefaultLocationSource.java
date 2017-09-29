package com.yayandroid.locationmanager.providers.locationprovider;

import android.content.Context;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;

import com.yayandroid.locationmanager.helper.UpdateRequest;
import com.yayandroid.locationmanager.helper.continuoustask.ContinuousTask;
import com.yayandroid.locationmanager.helper.continuoustask.ContinuousTask.ContinuousTaskRunner;

import java.util.Date;

class DefaultLocationSource {

    static final String PROVIDER_SWITCH_TASK = "providerSwitchTask";

    private LocationManager locationManager;
    private UpdateRequest updateRequest;
    private ContinuousTask cancelTask;

    void createLocationManager(Context context) {
        locationManager = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);
    }

    void createUpdateRequest(LocationListener locationListener) {
        updateRequest = new UpdateRequest(locationManager, locationListener);
    }

    void createProviderSwitchTask(ContinuousTaskRunner continuousTaskRunner) {
        this.cancelTask = new ContinuousTask(PROVIDER_SWITCH_TASK, continuousTaskRunner);
    }

    boolean isProviderEnabled(String provider) {
        return locationManager.isProviderEnabled(provider);
    }

    @SuppressWarnings("ResourceType")
    Location getLastKnownLocation(String provider) {
        return locationManager.getLastKnownLocation(provider);
    }

    @SuppressWarnings("ResourceType")
    void removeLocationUpdates(LocationListener locationListener) {
        locationManager.removeUpdates(locationListener);
    }

    void removeUpdateRequest() {
        updateRequest.release();
        updateRequest = null;
    }

    void removeSwitchTask() {
        cancelTask.stop();
        cancelTask = null;
    }

    boolean switchTaskIsRemoved() {
        return cancelTask == null;
    }

    boolean updateRequestIsRemoved() {
        return updateRequest == null;
    }

    ContinuousTask getProviderSwitchTask() {
        return cancelTask;
    }

    UpdateRequest getUpdateRequest() {
        return updateRequest;
    }

    boolean isLocationSufficient(Location location, long acceptableTimePeriod, float acceptableAccuracy) {
        if (location == null) return false;

        float givenAccuracy = location.getAccuracy();
        long givenTime = location.getTime();
        long minAcceptableTime = new Date().getTime() - acceptableTimePeriod;

        return minAcceptableTime <= givenTime && acceptableAccuracy >= givenAccuracy;
    }
}
