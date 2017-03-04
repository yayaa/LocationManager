package com.yayandroid.locationmanager.constants;

import android.support.annotation.IntDef;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

@IntDef({FailType.UNKNOWN, FailType.TIMEOUT, FailType.PERMISSION_DENIED, FailType.NETWORK_NOT_AVAILABLE,
      FailType.GP_SERVICES_NOT_AVAILABLE, FailType.GP_SERVICES_CONNECTION_FAIL, FailType.GP_SERVICES_SETTINGS_DIALOG,
      FailType.GP_SERVICES_SETTINGS_DENIED, FailType.VIEW_DETACHED, FailType.VIEW_NOT_REQUIRED_TYPE})
@Retention(RetentionPolicy.SOURCE)
public @interface FailType {

    int UNKNOWN = -1;
    int TIMEOUT = 1;
    int PERMISSION_DENIED = 2;
    int NETWORK_NOT_AVAILABLE = 3;
    int GP_SERVICES_NOT_AVAILABLE = 4;
    int GP_SERVICES_CONNECTION_FAIL = 5;
    int GP_SERVICES_SETTINGS_DIALOG = 6;
    int GP_SERVICES_SETTINGS_DENIED = 7;
    int VIEW_DETACHED = 8;
    int VIEW_NOT_REQUIRED_TYPE = 9;

}