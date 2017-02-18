package com.yayandroid.locationmanager.helper;

/*
 * Copyright Google Inc. All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */

import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.support.annotation.Nullable;
import android.support.annotation.StringRes;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.text.TextUtils;

import com.yayandroid.locationmanager.constants.RequestCode;

import java.util.ArrayList;
import java.util.List;

/**
 * Edited by Yahya Bayramoglu on 09/02/16.
 * Original Source: https://github.com/googlesamples/easypermissions
 */
public final class PermissionManager {

    private PermissionManager() {
        // No instance
    }

    public interface PermissionListener {

        /**
         * Given permissions are granted by User
         */
        void onPermissionsGranted(List<String> perms);

        /**
         * Given permissions are denied by User
         */
        void onPermissionsDenied(List<String> perms);

        /**
         * When User don't want to give permissions, rejected by rationalMessage dialog
         */
        void onPermissionRequestRejected();

    }

    public static boolean hasPermissions(Context context, String... requiredPermissions) {
        for (String permission : requiredPermissions) {
            if (ContextCompat.checkSelfPermission(context, permission) != PackageManager.PERMISSION_GRANTED) {
                return false;
            }
        }
        return true;
    }

    public static void requestPermissions(Object object, PermissionListener listener, String rationale, String... perms) {
        requestPermissions(object, listener, rationale, android.R.string.ok, android.R.string.cancel, perms);
    }

    public static void requestPermissions(final Object object, final PermissionListener listener, String rationale,
          @StringRes int positiveButton, @StringRes int negativeButton, final String... perms) {

        checkCallingObjectSuitability(object);

        boolean shouldShowRationale = false;
        for (int i = 0, size = perms.length; i < size; i++) {
            shouldShowRationale = shouldShowRationale || shouldShowRequestPermissionRationale(object, perms[i]);
        }

        Activity activity = getActivity(object);
        if (shouldShowRationale && !TextUtils.isEmpty(rationale) && activity != null) {
            AlertDialog dialog = new AlertDialog.Builder(activity)
                    .setMessage(rationale)
                    .setCancelable(false)
                    .setPositiveButton(positiveButton, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            executePermissionsRequest(object, perms, RequestCode.RUNTIME_PERMISSION);
                        }
                    })
                    .setNegativeButton(negativeButton, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            // Do nothing, user does not want to request
                            listener.onPermissionRequestRejected();
                        }
                    }).create();
            dialog.show();
        } else {
            executePermissionsRequest(object, perms, RequestCode.RUNTIME_PERMISSION);
        }
    }

    public static void onRequestPermissionsResult(PermissionListener callbacks, int requestCode,
          String[] permissions, int[] grantResults) {

        if (requestCode == RequestCode.RUNTIME_PERMISSION) {
            // Make a collection of granted and denied permissions from the request.
            ArrayList<String> granted = new ArrayList<>(10); // Default capacity
            ArrayList<String> denied = new ArrayList<>(10); // Default capacity

            for (int i = 0, size = permissions.length; i < size; i++) {
                if (grantResults[i] == PackageManager.PERMISSION_GRANTED) {
                    granted.add(permissions[i]);
                } else {
                    denied.add(permissions[i]);
                }
            }

            // Report granted permissions, if any.
            if (!granted.isEmpty()) callbacks.onPermissionsGranted(granted);

            // Report denied permissions, if any.
            if (!denied.isEmpty()) callbacks.onPermissionsDenied(denied);
        }
    }

    private static boolean shouldShowRequestPermissionRationale(Object object, String perm) {
        if (object instanceof Activity) {
            return ActivityCompat.shouldShowRequestPermissionRationale((Activity) object, perm);
        } else if (object instanceof Fragment) {
            return ((Fragment) object).shouldShowRequestPermissionRationale(perm);
        } else {
            return false;
        }
    }

    private static void executePermissionsRequest(Object object, String[] perms, int requestCode) {
        checkCallingObjectSuitability(object);

        if (object instanceof Activity) {
            ActivityCompat.requestPermissions((Activity) object, perms, requestCode);
        } else if (object instanceof Fragment) {
            ((Fragment) object).requestPermissions(perms, requestCode);
        }
    }

    @Nullable
    private static Activity getActivity(Object object) {
        if (object instanceof Activity) {
            return ((Activity) object);
        } else if (object instanceof Fragment) {
            return ((Fragment) object).getActivity();
        } else {
            return null;
        }
    }

    private static void checkCallingObjectSuitability(Object object) {
        // Make sure Object is an Activity or Fragment
        if (!((object instanceof Fragment) || (object instanceof Activity))) {
            throw new IllegalArgumentException("Caller must be an Activity or a Fragment.");
        }
    }
}