package com.yayandroid.locationmanager.mocks;

import android.app.Activity;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.android.gms.common.internal.Preconditions;
import com.google.android.gms.tasks.OnCanceledListener;
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

    public MockSimpleTask() {

    }

    public boolean isComplete() {
        return true;
    }

    public boolean isCanceled() {
        return false;
    }

    public boolean isSuccessful() {
        return this.error == null;
    }

    public TResult getResult() {
        if (this.error != null) {
            throw new RuntimeExecutionException(this.error);
        } else {
            return this.result;
        }
    }

    public <X extends Throwable> TResult getResult(@NonNull Class<X> var1) throws X {
        if (var1.isInstance(this.error)) {
            throw var1.cast(this.error);
        } else if (this.error != null) {
            throw new RuntimeExecutionException(this.error);
        } else {
            return this.result;
        }
    }

    @Nullable
    public Exception getException() {
        return this.error;
    }

    @NonNull
    public Task<TResult> addOnSuccessListener(@NonNull OnSuccessListener<? super TResult> var1) {
        if (isSuccessful()) {
            var1.onSuccess(result);
        }

        return this;
    }

    @NonNull
    public Task<TResult> addOnSuccessListener(@NonNull Executor var1, @NonNull OnSuccessListener<? super TResult> var2) {
        if (isSuccessful()) {
            var2.onSuccess(result);
        }

        return this;
    }

    @NonNull
    public Task<TResult> addOnSuccessListener(@NonNull Activity var1, @NonNull OnSuccessListener<? super TResult> var2) {
        if (isSuccessful()) {
            var2.onSuccess(result);
        }

        return this;
    }

    @NonNull
    public Task<TResult> addOnFailureListener(@NonNull OnFailureListener var1) {
        if (error != null && !isSuccessful()) {
            var1.onFailure(error);
        }

        return this;
    }

    @NonNull
    public Task<TResult> addOnFailureListener(@NonNull Executor var1, @NonNull OnFailureListener var2) {
        if (error != null && !isSuccessful()) {
            var2.onFailure(error);
        }

        return this;
    }

    @NonNull
    public Task<TResult> addOnFailureListener(@NonNull Activity var1, @NonNull OnFailureListener var2) {
        if (error != null && !isSuccessful()) {
            var2.onFailure(error);
        }

        return this;
    }

    @NonNull
    public Task<TResult> addOnCompleteListener(@NonNull OnCompleteListener<TResult> var1) {
        if (isComplete()) {
            var1.onComplete(this);
        }

        return this;
    }

    @NonNull
    public Task<TResult> addOnCompleteListener(@NonNull Executor var1, @NonNull OnCompleteListener<TResult> var2) {
        if (isComplete()) {
            var2.onComplete(this);
        }

        return this;
    }

    @NonNull
    public Task<TResult> addOnCompleteListener(@NonNull Activity var1, @NonNull OnCompleteListener<TResult> var2) {
        if (isComplete()) {
            var2.onComplete(this);
        }

        return this;
    }

    @NonNull
    public Task<TResult> addOnCanceledListener(@NonNull OnCanceledListener var1) {
        if (isCanceled()) {
            var1.onCanceled();
        }

        return this;
    }

    @NonNull
    public Task<TResult> addOnCanceledListener(@NonNull Executor var1, @NonNull OnCanceledListener var2) {
        if (isCanceled()) {
            var2.onCanceled();
        }

        return this;
    }

    @NonNull
    public Task<TResult> addOnCanceledListener(@NonNull Activity var1, @NonNull OnCanceledListener var2) {
        if (isCanceled()) {
            var2.onCanceled();
        }

        return this;
    }

    public void setResult(@Nullable TResult var1) {
        this.result = var1;
    }

    public boolean trySetResult(TResult var1) {
        this.result = var1;

        return true;
    }

    public void setException(@NonNull Exception var1) {
        Preconditions.checkNotNull(var1, "Exception must not be null");

        this.error = var1;
    }

    public boolean trySetException(@NonNull Exception var1) {
        Preconditions.checkNotNull(var1, "Exception must not be null");
        this.error = var1;

        return true;
    }

}
