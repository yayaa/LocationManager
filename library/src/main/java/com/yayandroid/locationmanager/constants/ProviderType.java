package com.yayandroid.locationmanager.constants;

import android.support.annotation.IntDef;

public class ProviderType {

    public static final int NONE = 0;
    public static final int GOOGLE_PLAY_SERVICES = 1;
    public static final int GPS = 2;
    public static final int NETWORK = 3;

    /**
     * Covers both GPS and NETWORK
     */
    public static final int DEFAULT_PROVIDERS = 4;

    @IntDef({NONE, GOOGLE_PLAY_SERVICES, GPS, NETWORK, DEFAULT_PROVIDERS})
    public @interface Source {
    }

}
