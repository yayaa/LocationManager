package com.yayandroid.locationmanager.mocks;

import android.app.Activity;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.RuntimeExecutionException;
import com.google.android.gms.tasks.Task;

import java.util.concurrent.Executor;

public class MockSimpleTask<TResult> extends Task<TResult> {

    @Nullable
    protected TResult result;

    @Nullable
    protected Exception error;

    public MockSimpleTask(@Nullable TResult result) {
        this.result = result;
    }

    public MockSimpleTask(@NonNull Exception error) {
        this.error = error;
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
        if (isSuccessful()) {
            listener.onSuccess(result);
        }

        return this;
    }

    @NonNull
    @Override
    public Task<TResult> addOnSuccessListener(@NonNull Executor executor, @NonNull OnSuccessListener<? super TResult> listener) {
        if (isSuccessful()) {
            listener.onSuccess(result);
        }

        return this;
    }

    @NonNull
    @Override
    public Task<TResult> addOnSuccessListener(@NonNull Activity activity, @NonNull OnSuccessListener<? super TResult> listener) {
        if (isSuccessful()) {
            listener.onSuccess(result);
        }

        return this;
    }

    @NonNull
    @Override
    public Task<TResult> addOnFailureListener(@NonNull OnFailureListener listener) {
        if (error != null && !isSuccessful()) {
            listener.onFailure(error);
        }

        return this;
    }

    @NonNull
    @Override
    public Task<TResult> addOnFailureListener(@NonNull Executor executor, @NonNull OnFailureListener listener) {
        if (error != null && !isSuccessful()) {
            listener.onFailure(error);
        }

        return this;
    }

    @NonNull
    @Override
    public Task<TResult> addOnFailureListener(@NonNull Activity activity, @NonNull OnFailureListener listener) {
        if (error != null && !isSuccessful()) {
            listener.onFailure(error);
        }

        return this;
    }

    @NonNull
    @Override
    public Task<TResult> addOnCompleteListener(@NonNull OnCompleteListener<TResult> listener) {
        if (isComplete()) {
            listener.onComplete(this);
        }

        return this;
    }

}
