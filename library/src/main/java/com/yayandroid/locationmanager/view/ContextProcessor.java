package com.yayandroid.locationmanager.view;

import android.app.Activity;
import android.content.Context;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;

import java.lang.ref.WeakReference;

public class ContextProcessor {

    private WeakReference<Context> weakContext;
    private WeakReference<Fragment> weakFragment;

    /**
     * In order to use in Activity, Application or Service
     */
    public ContextProcessor(Context context) {
        weakContext = new WeakReference<>(context);
        weakFragment = new WeakReference<>(null);
    }

    /**
     * In order to use in Fragment
     */
    public ContextProcessor(Fragment fragment) {
        weakContext = new WeakReference<>(null);
        weakFragment = new WeakReference<>(fragment);
    }

    @Nullable
    public Fragment getFragment() {
        return weakFragment.get();
    }

    @Nullable
    public Context getContext() {
        if (weakContext.get() != null) return weakContext.get();
        if (weakFragment.get() != null && weakFragment.get().getContext() != null) return weakFragment.get().getContext();
        return null;
    }

    @Nullable
    public Activity getActivity() {
        if (getContext() != null && getContext() instanceof Activity) return (Activity) getContext();
        if (getFragment() != null && getFragment().getActivity() != null) return getFragment().getActivity();
        return null;
    }
}
