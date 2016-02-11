package com.yayandroid.locationmanager.helper;

import android.util.Log;

import com.yayandroid.locationmanager.constants.LogType;

/**
 * Created by Yahya Bayramoglu on 09/02/16.
 */
public class LogUtils {

    private static int logType = LogType.IMPORTANT;

    public static void setLogType(@LogType.Level int logType) {
        LogUtils.logType = logType;
    }

    public static void logD(String message, @LogType.Level int type) {
        if (type <= logType) {
            Log.d(getClassName(), message);
        }
    }

    public static void logE(String message, @LogType.Level int type) {
        if (type <= logType) {
            Log.e(getClassName(), message);
        }
    }

    public static void logI(String message, @LogType.Level int type) {
        if (type <= logType) {
            Log.i(getClassName(), message);
        }
    }

    public static void logV(String message, @LogType.Level int type) {
        if (type <= logType) {
            Log.v(getClassName(), message);
        }
    }

    public static void logW(String message, @LogType.Level int type) {
        if (type <= logType) {
            Log.w(getClassName(), message);
        }
    }

    private static String getClassName() {
        StackTraceElement[] trace = Thread.currentThread().getStackTrace();
        StackTraceElement relevantTrace = trace[4];
        String className = relevantTrace.getClassName();
        int lastIndex = className.lastIndexOf(".");
        return className.substring(lastIndex + 1);
    }

}