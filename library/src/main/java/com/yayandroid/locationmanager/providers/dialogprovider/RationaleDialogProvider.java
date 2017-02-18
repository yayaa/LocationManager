package com.yayandroid.locationmanager.providers.dialogprovider;

import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.support.annotation.NonNull;
import android.support.v7.app.AlertDialog;
import android.text.TextUtils;

public class RationaleDialogProvider extends DialogProvider implements DialogInterface.OnClickListener {

    private String rationale;

    public RationaleDialogProvider(String rationale) {
        this.rationale = rationale;
    }

    @Override
    public Dialog getDialog(@NonNull Context context) {
        return new AlertDialog.Builder(context)
              .setMessage(rationale)
              .setCancelable(false)
              .setPositiveButton(android.R.string.ok, this)
              .setNegativeButton(android.R.string.cancel, this)
              .create();
    }

    @Override
    public boolean shouldShown() {
        return !TextUtils.isEmpty(rationale);
    }

    @Override
    public void onClick(DialogInterface dialog, int which) {
        switch (which) {
            case DialogInterface.BUTTON_POSITIVE: {
                if (dialogListener != null) dialogListener.onPositiveButtonClick();
                break;
            }
            case DialogInterface.BUTTON_NEGATIVE: {
                if (dialogListener != null) dialogListener.onNegativeButtonClick();
                break;
            }
        }
    }
}
