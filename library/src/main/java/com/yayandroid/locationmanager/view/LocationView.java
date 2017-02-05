package com.yayandroid.locationmanager.view;

import android.app.Activity;
import android.content.Context;
import android.support.annotation.Nullable;

import java.lang.ref.WeakReference;

public class LocationView {

    private WeakReference<Context> weakContext;

    public LocationView(Context context) {
        this.weakContext = new WeakReference<>(context);
    }

    public @Nullable Activity getActivity() {
        return isActivityExist() ? ((Activity) getContext()) : null;
    }

    public @Nullable Context getContext() {
        return weakContext.get();
    }

    public boolean isActivityExist() {
        return getContext() != null && getContext() instanceof Activity;
    }

    public boolean isContextExist() {
        return getContext() != null;
    }
}
