package com.yayandroid.locationmanager.providers.locationprovider;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.support.annotation.CallSuper;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;

import com.yayandroid.locationmanager.listener.LocationListener;
import com.yayandroid.locationmanager.configuration.LocationConfiguration;
import com.yayandroid.locationmanager.view.ContextProcessor;

import java.lang.ref.WeakReference;

public abstract class LocationProvider {

    private boolean isWaiting = false;
    private LocationConfiguration configuration;
    private WeakReference<ContextProcessor> weakContextProcessor;
    private WeakReference<LocationListener> weakLocationListener;

    @CallSuper public void configure(ContextProcessor contextProcessor, LocationConfiguration configuration) {
        this.weakContextProcessor = new WeakReference<>(contextProcessor);
        this.configuration = configuration;
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
    public void notifyTo(LocationListener listener) {
        this.weakLocationListener = new WeakReference<>(listener);
    }

    /**
     * Override when you need to handle activityResult such as listening for GPS activation
     */
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
    }

    /**
     * To remove location updates while getting from GPS or Network Provider
     */
    @CallSuper public void onDestroy() {
        configuration = null;
        weakContextProcessor.clear();
        weakLocationListener.clear();
    }

    public void onPause() {
    }

    public void onResume() {
    }

    protected LocationConfiguration getConfiguration() {
        return configuration;
    }

    @Nullable protected LocationListener getListener() {
        return weakLocationListener.get();
    }

    @Nullable protected Context getContext() {
        return weakContextProcessor.get() == null ? null : weakContextProcessor.get().getContext();
    }

    @Nullable protected Activity getActivity() {
        return weakContextProcessor.get() == null ? null : weakContextProcessor.get().getActivity();
    }

    @Nullable protected Fragment getFragment() {
        return weakContextProcessor.get() == null ? null : weakContextProcessor.get().getFragment();
    }

}