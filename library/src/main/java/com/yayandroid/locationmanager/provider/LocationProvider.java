package com.yayandroid.locationmanager.provider;

import android.content.Intent;

import com.yayandroid.locationmanager.LocationReceiver;
import com.yayandroid.locationmanager.configuration.LocationConfiguration;
import com.yayandroid.locationmanager.view.ContextProcessor;

public abstract class LocationProvider {

    private boolean isWaiting = false;
    protected LocationConfiguration configuration;
    protected ContextProcessor contextProcessor;
    protected LocationReceiver listener;

    public void configure(ContextProcessor contextProcessor, LocationConfiguration configuration) {
        this.contextProcessor = contextProcessor;
        this.configuration = configuration;
        onCreate();
    }

    /**
     * Return true if the provider needs to listen for activityResult, false otherwise.
     */
    public abstract boolean requiresActivityResult();

    /**
     * This method will be used to determine whether any LocationProvider
     * is currently displaying dialog or something.
     */
    public abstract boolean isDialogShowing();

    /**
     * This is where your provider actually starts working
     */
    public abstract void get();

    /**
     * This provider is asked to be canceled all tasks currently running
     * and remove all location update listeners
     */
    public abstract void cancel();

    /**
     * Call this method while you begin to process getting location
     * and call it when at least one location is received
     */
    public void setWaiting(boolean waiting) {
        this.isWaiting = waiting;
    }

    /**
     * Returns waiting state
     */
    public boolean isWaiting() {
        return isWaiting;
    }

    /**
     * While you are providing location by yourself,
     * then you have to invoke methods
     */
    public void notifyTo(LocationReceiver listener) {
        this.listener = listener;
    }

    /**
     * Override when you need to handle activityResult such as listening for GPS activation
     */
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
    }

    /**
     * Now you have both activity and configuration available,
     * do whatever you need to do before getting location.
     */
    public void onCreate() {
    }

    /**
     * To remove location updates while getting from GPS or Network Provider
     */
    public void onDestroy() {
        // Release instances not to cause leak
        this.configuration = null;
        this.listener = null;
    }

    public void onPause() {
    }

    public void onResume() {
    }

}