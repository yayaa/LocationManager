package com.yayandroid.locationmanager.mocks;

import androidx.annotation.NonNull;

import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.common.api.ResolvableApiException;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.LocationSettingsResponse;
import com.google.android.gms.location.LocationSettingsResult;
import com.google.android.gms.location.LocationSettingsStatusCodes;

public class MockLocationSettingsResponseTask extends MockSimpleTask<LocationSettingsResponse> {

    public MockLocationSettingsResponseTask(int statusCode, boolean isError) {
        Status status = new Status(statusCode, null, null);

        if (isError) {
            if (statusCode == LocationSettingsStatusCodes.RESOLUTION_REQUIRED) {
                error = new ResolvableApiException(status);
            } else {
                error = new ApiException(status);
            }
        } else {
            result = new LocationSettingsResponse();

            result.setResult(new LocationSettingsResult(status));
        }
    }

    public MockLocationSettingsResponseTask(@NonNull Exception error) {
        super(error);
    }

}
