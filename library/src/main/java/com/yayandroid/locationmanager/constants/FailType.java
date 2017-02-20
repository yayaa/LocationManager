package com.yayandroid.locationmanager.constants;

import android.support.annotation.IntDef;

public class FailType {

    public static final int UNKNOWN = -1;
    public static final int TIMEOUT = 1;
    public static final int PERMISSION_DENIED = 2;
    public static final int NETWORK_NOT_AVAILABLE = 3;
    public static final int GP_SERVICES_NOT_AVAILABLE = 4;
    public static final int GP_SERVICES_CONNECTION_FAIL = 5;
    public static final int GP_SERVICES_SETTINGS_DIALOG = 6;
    public static final int GP_SERVICES_SETTINGS_DENIED = 7;
    public static final int VIEW_DETACHED = 8;
    public static final int VIEW_NOT_REQUIRED_TYPE = 9;

    @IntDef({UNKNOWN, TIMEOUT, PERMISSION_DENIED, NETWORK_NOT_AVAILABLE, GP_SERVICES_NOT_AVAILABLE,
          GP_SERVICES_CONNECTION_FAIL, GP_SERVICES_SETTINGS_DIALOG, GP_SERVICES_SETTINGS_DENIED, VIEW_DETACHED,
          VIEW_NOT_REQUIRED_TYPE})
    public @interface Reason {
    }
}