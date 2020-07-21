package com.yayandroid.locationmanager.mocks;

import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.common.api.ResolvableApiException;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.LocationSettingsResponse;
import com.google.android.gms.location.LocationSettingsResult;
import com.google.android.gms.location.LocationSettingsStatusCodes;

public class MockLocationSettingsResponseTask extends MockSimpleTask<LocationSettingsResponse> {

    public MockLocationSettingsResponseTask(int statusCode) {
        Status status = new Status(statusCode, null, null);

        result = new LocationSettingsResponse();

        result.setResult(new LocationSettingsResult(status));

        switch (statusCode) {
            case LocationSettingsStatusCodes.SETTINGS_CHANGE_UNAVAILABLE:
                error = new ApiException(status);

                break;
            case LocationSettingsStatusCodes.RESOLUTION_REQUIRED:
                error = new ResolvableApiException(status);

                break;
        }
    }

}
