package com.yayandroid.locationmanager;

import android.content.Intent;
import android.location.Location;
import android.location.LocationListener;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

import com.yayandroid.locationmanager.configuration.LocationConfiguration;

public abstract class LocationBaseActivity extends AppCompatActivity implements LocationListener {

    private LocationManager locationManager;

    public abstract LocationConfiguration getLocationConfiguration();

    public abstract void onLocationFailed(int failType);

    public abstract void onLocationChanged(Location location);

    protected LocationManager getLocationManager() {
        return locationManager;
    }

    public void getLocation() {
        if (locationManager != null) {
            locationManager.get();
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        locationManager = new LocationManager(getLocationConfiguration())
              .on(this)
              .notify(locationReceiver);
    }

    @Override
    protected void onDestroy() {
        locationManager.onDestroy();
        super.onDestroy();
    }

    @Override
    protected void onPause() {
        super.onPause();
        locationManager.onPause();
    }

    @Override
    protected void onResume() {
        super.onResume();
        locationManager.onResume();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        locationManager.onActivityResult(requestCode, resultCode, data);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        locationManager.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {
    }

    @Override
    public void onProviderEnabled(String provider) {
    }

    @Override
    public void onProviderDisabled(String provider) {
    }

    private final LocationReceiver locationReceiver = new LocationReceiver() {

        @Override
        public void onLocationChanged(Location location) {
            LocationBaseActivity.this.onLocationChanged(location);
        }

        @Override
        public void onLocationFailed(int failType) {
            LocationBaseActivity.this.onLocationFailed(failType);
        }

        @Override
        public void onStatusChanged(String provider, int status, Bundle extras) {
            LocationBaseActivity.this.onStatusChanged(provider, status, extras);
        }

        @Override
        public void onProviderEnabled(String provider) {
            LocationBaseActivity.this.onProviderEnabled(provider);
        }

        @Override
        public void onProviderDisabled(String provider) {
            LocationBaseActivity.this.onProviderDisabled(provider);
        }
    };

}
