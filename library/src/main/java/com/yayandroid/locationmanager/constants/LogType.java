package com.yayandroid.locationmanager.constants;

import android.support.annotation.IntDef;

/**
 * Created by Yahya Bayramoglu on 10/02/16.
 */
public class LogType {

    public static final int NONE = 0;
    public static final int IMPORTANT = 1;
    public static final int GENERAL = 2;

    @IntDef({IMPORTANT, GENERAL, NONE})
    public @interface Level {
    }

}