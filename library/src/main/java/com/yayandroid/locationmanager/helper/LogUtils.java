package com.yayandroid.locationmanager.helper;

import android.util.Log;

public final class LogUtils {

    private static boolean isEnabled = false;

    private LogUtils() {
        // No instance
    }

    public static void enable(boolean isEnabled) {
        LogUtils.isEnabled = isEnabled;
    }

    public static void logD(String message) {
        if (isEnabled) Log.d(getClassName(), message);
    }

    public static void logE(String message) {
        if (isEnabled) Log.e(getClassName(), message);
    }

    public static void logI(String message) {
        if (isEnabled) Log.i(getClassName(), message);
    }

    public static void logV(String message) {
        if (isEnabled) Log.v(getClassName(), message);
    }

    public static void logW(String message) {
        if (isEnabled) Log.w(getClassName(), message);
    }

    private static String getClassName() {
        StackTraceElement[] trace = Thread.currentThread().getStackTrace();
        StackTraceElement relevantTrace = trace[4];
        String className = relevantTrace.getClassName();
        int lastIndex = className.lastIndexOf('.');
        return className.substring(lastIndex + 1);
    }
}