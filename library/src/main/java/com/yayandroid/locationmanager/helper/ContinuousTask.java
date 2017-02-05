package com.yayandroid.locationmanager.helper;

import android.os.Handler;

import com.yayandroid.locationmanager.constants.LogType;

import java.lang.ref.WeakReference;

public class ContinuousTask extends Handler implements Runnable {

    private final static long NONE = Long.MIN_VALUE;

    private final String taskId;
    private final WeakReference<ContinuousTaskRunner> weekContinuousTaskRunner;

    private long requiredDelay = NONE;
    private long initialTime = NONE;
    private long remainingTime = NONE;

    private boolean isSet = false;

    public interface ContinuousTaskRunner {
        /**
         * Callback to take action when scheduled time is arrived.
         * Called with given taskId in order to distinguish which task should be run,
         * in case of same {@linkplain ContinuousTaskRunner} passed to multiple Tasks
         */
        void runScheduledTask(String taskId);
    }

    public ContinuousTask(String taskId, ContinuousTaskRunner continuousTaskRunner) {
        this.taskId = taskId;
        weekContinuousTaskRunner = new WeakReference<>(continuousTaskRunner);
    }

    public void delayed(long delay) {
        requiredDelay = delay;
        remainingTime = requiredDelay;
        initialTime = System.currentTimeMillis();

        set(delay);
    }

    public void pause() {
        if (requiredDelay != NONE) {
            release();
            remainingTime = requiredDelay - (System.currentTimeMillis() - initialTime);
        }
    }

    public void resume() {
        if (remainingTime != NONE) {
            set(remainingTime);
        }
    }

    public void stop() {
        release();

        requiredDelay = NONE;
        initialTime = NONE;
        remainingTime = NONE;
    }

    @Override
    public void run() {
        if (weekContinuousTaskRunner.get() != null) {
            weekContinuousTaskRunner.get().runScheduledTask(taskId);
        } else {
            LogUtils.logE("Something went wrong and task failed.", LogType.IMPORTANT);
        }
    }

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
