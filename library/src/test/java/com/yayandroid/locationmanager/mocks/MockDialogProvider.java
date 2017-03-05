package com.yayandroid.locationmanager.mocks;

import android.app.Dialog;
import android.content.Context;
import android.support.annotation.NonNull;

import com.yayandroid.locationmanager.providers.dialogprovider.DialogProvider;

public final class MockDialogProvider extends DialogProvider {

    private String message;

    public MockDialogProvider(String message) {
        this.message = message;
    }

    public String message() {
        return message;
    }

    @Override
    public Dialog getDialog(@NonNull Context context) {
        return null;
    }
}