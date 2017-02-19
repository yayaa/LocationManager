package com.yayandroid.locationmanager.providers.dialogprovider;

import android.app.Dialog;
import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.yayandroid.locationmanager.LocationManager;
import com.yayandroid.locationmanager.listener.DialogListener;
import com.yayandroid.locationmanager.providers.permissionprovider.DefaultPermissionProvider;

public abstract class DialogProvider {

    protected DialogListener dialogListener;

    /**
     * Create a dialog object on given context
     *
     * @param context in which the dialog should run
     * @return dialog object to display
     */
    public abstract Dialog getDialog(@NonNull Context context);

    /**
     * Sets a {@linkplain DialogListener} to provide pre-defined actions to the component which uses this dialog
     *
     * This method will be called by {@linkplain DefaultPermissionProvider} internally, if it is in use.
     *
     * @param dialogListener will be used to notify on specific actions
     */
    public void setDialogListener(@Nullable DialogListener dialogListener) {
        this.dialogListener = dialogListener;
    }
}
