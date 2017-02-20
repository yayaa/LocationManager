package com.yayandroid.locationmanager.constants;

import android.support.annotation.IntDef;

public class LogType {

    public static final int NONE = 0;
    public static final int IMPORTANT = 1;
    public static final int GENERAL = 2;

    @IntDef({IMPORTANT, GENERAL, NONE})
    public @interface Level {
    }

}