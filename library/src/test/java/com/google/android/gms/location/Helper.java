package com.google.android.gms.location;

import androidx.annotation.NonNull;

public class Helper {

    @NonNull
    public static LocationAvailability getLocationAvailability(boolean isLocationAvailable) {
        // LocationAvailability#isLocationAvailable() returns true, if first argument is less than 1000.
        if (isLocationAvailable) {
            return new LocationAvailability(0, 0, 0, 0, null);
        } else {
            return new LocationAvailability(1000, 0, 0, 0, null);
        }
    }

}
