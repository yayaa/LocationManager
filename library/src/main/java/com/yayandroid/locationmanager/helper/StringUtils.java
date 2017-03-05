package com.yayandroid.locationmanager.helper;

import android.support.annotation.Nullable;

public final class StringUtils {

    private StringUtils() {
        // no instance
    }

    public static boolean isEmpty(@Nullable CharSequence str) {
        return str == null || str.length() == 0;
    }

    public static boolean isNotEmpty(@Nullable CharSequence str) {
        return str != null && str.length() > 0;
    }

}
