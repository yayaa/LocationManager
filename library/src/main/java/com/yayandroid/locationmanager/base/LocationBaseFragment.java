package com.yayandroid.locationmanager.base;

import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.CallSuper;
import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import com.yayandroid.locationmanager.LocationManager;
import com.yayandroid.locationmanager.configuration.LocationConfiguration;
import com.yayandroid.locationmanager.constants.ProcessType;
import com.yayandroid.locationmanager.listener.LocationListener;

public abstract class LocationBaseFragment extends Fragment implements LocationListener {

    private LocationManager locationManager;

    public abstract LocationConfiguration getLocationConfiguration();

    protected LocationManager getLocationManager() {
        return locationManager;
    }

    protected void getLocation() {
        if (locationManager != null) {
            locationManager.get();
        } else {
            throw new IllegalStateException("locationManager is null. "
                  + "Make sure you call super.initialize before attempting to getLocation");
        }
    }

    @CallSuper
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        locationManager = new LocationManager.Builder(getContext().getApplicationContext())
              .configuration(getLocationConfiguration())
              .fragment(this)
              .notify(this)
              .build();
    }

    @CallSuper
    @Override
    public void onDestroy() {
        locationManager.onDestroy();
        super.onDestroy();
    }

    @CallSuper
    @Override
    public void onPause() {
        locationManager.onPause();
        super.onPause();
    }

    @CallSuper
    @Override
    public void onResume() {
        super.onResume();
        locationManager.onResume();
    }

    @CallSuper
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        locationManager.onActivityResult(requestCode, resultCode, data);
    }
    
    @CallSuper
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        locationManager.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }

    @Override
    public void onProcessTypeChanged(@ProcessType int processType) {
        // override if needed
    }

    @Override
    public void onPermissionGranted(boolean alreadyHadPermission) {
        // override if needed
    }

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {
        // override if needed
    }

    @Override
    public void onProviderEnabled(String provider) {
        // override if needed
    }

    @Override
    public void onProviderDisabled(String provider) {
        // override if needed
    }
}
