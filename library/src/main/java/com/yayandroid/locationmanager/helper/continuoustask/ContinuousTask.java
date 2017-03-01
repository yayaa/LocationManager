package com.yayandroid.locationmanager.helper.continuoustask;

import android.os.Handler;
import android.support.annotation.NonNull;

import com.yayandroid.locationmanager.helper.LogUtils;

import java.lang.ref.WeakReference;

public class ContinuousTask extends Handler implements Runnable {

    private final String taskId;
    private final WeakReference<ContinuousTaskRunner> weekContinuousTaskRunner;
    private final ContinuousTaskScheduler continuousTaskScheduler;

    public interface ContinuousTaskRunner {
        /**
         * Callback to take action when scheduled time is arrived.
         * Called with given taskId in order to distinguish which task should be run,
         * in case of same {@linkplain ContinuousTaskRunner} passed to multiple Tasks
         */
        void runScheduledTask(@NonNull String taskId);
    }

    public ContinuousTask(@NonNull String taskId, @NonNull ContinuousTaskRunner continuousTaskRunner) {
        this.taskId = taskId;
        continuousTaskScheduler = new ContinuousTaskScheduler(this);
        weekContinuousTaskRunner = new WeakReference<>(continuousTaskRunner);
    }

    public void delayed(long delay) {
        continuousTaskScheduler.delayed(delay);
    }

    public void pause() {
        continuousTaskScheduler.onPause();
    }

    public void resume() {
        continuousTaskScheduler.onResume();
    }

    public void stop() {
        continuousTaskScheduler.onStop();
    }

    @Override
    public void run() {
        if (weekContinuousTaskRunner.get() != null) {
            weekContinuousTaskRunner.get().runScheduledTask(taskId);
        } else {
            LogUtils.logE("Something went wrong and task failed.");
        }
        continuousTaskScheduler.clean();
    }

    void schedule(long delay) {
        postDelayed(this, delay);
    }

    void unregister() {
        removeCallbacks(this);
    }

    long getCurrentTime() {
        return System.currentTimeMillis();
    }
}
