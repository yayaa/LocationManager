package com.yayandroid.locationmanager.sample.fragment;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;

import com.yayandroid.locationmanager.sample.R;

public class SampleFragmentActivity extends AppCompatActivity {

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sample_fragment);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        // This is necessary because SettingsApi requires Activity, and because it calls startActivityForResult from the
        // activity, but not fragment, fragment doesn't receive onActivityResult callback.
        SampleFragment sampleFragment = (SampleFragment) getSupportFragmentManager().findFragmentById(R.id.sample_fragment);
        sampleFragment.onActivityResult(requestCode, resultCode, data);
    }
}
