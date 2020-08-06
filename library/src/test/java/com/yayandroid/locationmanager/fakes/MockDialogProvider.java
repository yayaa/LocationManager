package com.yayandroid.locationmanager.fakes;

import android.app.Dialog;
import android.content.Context;

import androidx.annotation.NonNull;

import com.yayandroid.locationmanager.providers.dialogprovider.DialogProvider;

import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

public final class MockDialogProvider extends DialogProvider {

    private String message;
    @Mock Dialog dialog;

    public MockDialogProvider(String message) {
        this.message = message;
        MockitoAnnotations.initMocks(this);
    }

    public String message() {
        return message;
    }

    @Override
    public Dialog getDialog(@NonNull Context context) {
        return dialog;
    }
}