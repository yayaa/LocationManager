package com.yayandroid.locationmanager.fakes;

import android.app.Activity;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.RuntimeExecutionException;
import com.google.android.gms.tasks.Task;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executor;

public class FakeSimpleTask<TResult> extends Task<TResult> {

    @Nullable
    protected TResult result;

    @Nullable
    protected Exception error;

    private final List<OnSuccessListener<? super TResult>> onSuccessListeners = new ArrayList<>();

    private final List<OnFailureListener> onFailureListeners = new ArrayList<>();

    private final List<OnCompleteListener<TResult>> onCompleteListeners = new ArrayList<>();

    public FakeSimpleTask() {
    }

    public void success(@Nullable TResult result) {
        this.result = result;

        for (OnSuccessListener<? super TResult> onSuccessListener : onSuccessListeners) {
            onSuccessListener.onSuccess(result);
        }

        for (OnCompleteListener<TResult> completeListener : onCompleteListeners) {
            completeListener.onComplete(this);
        }
    }

    public void error(@NonNull Exception error) {
        this.error = error;

        for (OnFailureListener onFailureListener : onFailureListeners) {
            onFailureListener.onFailure(error);
        }

        for (OnCompleteListener<TResult> completeListener : onCompleteListeners) {
            completeListener.onComplete(this);
        }
    }

    @Override
    public boolean isComplete() {
        return true;
    }

    @Override
    public boolean isCanceled() {
        return false;
    }

    @Override
    public boolean isSuccessful() {
        return this.error == null;
    }

    @Override
    public TResult getResult() {
        if (this.error != null) {
            throw new RuntimeExecutionException(this.error);
        } else {
            return this.result;
        }
    }

    @Override
    public <X extends Throwable> TResult getResult(@NonNull Class<X> throwableClass) throws X {
        if (throwableClass.isInstance(this.error)) {
            throw throwableClass.cast(this.error);
        } else if (this.error != null) {
            throw new RuntimeExecutionException(this.error);
        } else {
            return this.result;
        }
    }

    @Nullable
    @Override
    public Exception getException() {
        return this.error;
    }

    @NonNull
    @Override
    public Task<TResult> addOnSuccessListener(@NonNull OnSuccessListener<? super TResult> listener) {
        onSuccessListeners.add(listener);

        return this;
    }

    @NonNull
    @Override
    public Task<TResult> addOnSuccessListener(@NonNull Executor executor, @NonNull OnSuccessListener<? super TResult> listener) {
        onSuccessListeners.add(listener);

        return this;
    }

    @NonNull
    @Override
    public Task<TResult> addOnSuccessListener(@NonNull Activity activity, @NonNull OnSuccessListener<? super TResult> listener) {
        onSuccessListeners.add(listener);

        return this;
    }

    @NonNull
    @Override
    public Task<TResult> addOnFailureListener(@NonNull OnFailureListener listener) {
        onFailureListeners.add(listener);

        return this;
    }

    @NonNull
    @Override
    public Task<TResult> addOnFailureListener(@NonNull Executor executor, @NonNull OnFailureListener listener) {
        onFailureListeners.add(listener);

        return this;
    }

    @NonNull
    @Override
    public Task<TResult> addOnFailureListener(@NonNull Activity activity, @NonNull OnFailureListener listener) {
        onFailureListeners.add(listener);

        return this;
    }

    @NonNull
    @Override
    public Task<TResult> addOnCompleteListener(@NonNull OnCompleteListener<TResult> listener) {
        onCompleteListeners.add(listener);

        return this;
    }

}
