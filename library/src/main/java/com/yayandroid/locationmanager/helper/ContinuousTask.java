package com.yayandroid.locationmanager.helper;

import android.os.Handler;

/**
 * Created by Yahya Bayramoglu on 11/02/16.
 */
public abstract class ContinuousTask extends Handler implements Runnable {

    private boolean isSet = false;
    private long NONE = Long.MIN_VALUE;
    private long requiredDelay = NONE, initialTime = NONE, remainingTime = NONE;

    public void delayed(long delay) {
        this.requiredDelay = delay;
        this.remainingTime = requiredDelay;
        this.initialTime = System.currentTimeMillis();

        set(delay);
    }

    public void pause() {
        if (requiredDelay != NONE) {
            release();
            this.remainingTime = requiredDelay - (System.currentTimeMillis() - initialTime);
        }
    }

    public void resume() {
        if (remainingTime != NONE) {
            set(remainingTime);
        }
    }

    public void stop() {
        release();

        this.requiredDelay = NONE;
        this.initialTime = NONE;
        this.remainingTime = NONE;
    }

    public abstract void run();

    private void set(long delay) {
        if (!isSet) {
            postDelayed(this, delay);
            isSet = true;
        }
    }

    private void release() {
        removeCallbacks(this);
        isSet = false;
    }

}
