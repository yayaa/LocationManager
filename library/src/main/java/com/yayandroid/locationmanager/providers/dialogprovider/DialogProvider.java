package com.yayandroid.locationmanager.providers.dialogprovider;

import android.app.Dialog;
import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.yayandroid.locationmanager.listener.DialogListener;

public abstract class DialogProvider {

    protected DialogListener dialogListener;

    public abstract Dialog getDialog(@NonNull Context context);

    public abstract boolean shouldShown();

    public void setDialogListener(@Nullable DialogListener dialogListener) {
        this.dialogListener = dialogListener;
    }
}
