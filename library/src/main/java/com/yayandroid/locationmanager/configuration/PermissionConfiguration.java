package com.yayandroid.locationmanager.configuration;

import android.text.TextUtils;

import com.yayandroid.locationmanager.providers.dialogprovider.DialogProvider;
import com.yayandroid.locationmanager.providers.dialogprovider.SimpleMessageDialogProvider;
import com.yayandroid.locationmanager.providers.permissionprovider.DefaultPermissionProvider;
import com.yayandroid.locationmanager.providers.permissionprovider.PermissionProvider;

public class PermissionConfiguration {

    private final String rationalMessage;
    private final String[] requiredPermissions;
    private final PermissionProvider permissionProvider;

    private PermissionConfiguration(Builder builder) {
        this.permissionProvider = builder.permissionProvider;
        this.rationalMessage = builder.rationalMessage;
        this.requiredPermissions = builder.requiredPermissions;
    }

    public PermissionConfiguration.Builder newBuilder() {
        return new PermissionConfiguration.Builder()
              .permissionProvider(permissionProvider)
              .rationalMessage(rationalMessage)
              .requiredPermissions(requiredPermissions);
    }

    // region Getters
    public String rationalMessage() {
        return rationalMessage;
    }

    public String[] requiredPermissions() {
        return requiredPermissions;
    }

    public PermissionProvider permissionProvider() {
        return permissionProvider;
    }
    // endregion

    public static class Builder {

        private String rationalMessage = Defaults.EMPTY_STRING;
        private String[] requiredPermissions = Defaults.LOCATION_PERMISSIONS;
        private DialogProvider rationaleDialogProvider;
        private PermissionProvider permissionProvider;

        /**
         * Indicates what to display when user needs to see a rational dialog for RuntimePermission.
         * There is no default value, so if you do not set this, user will not see any rationale dialog.
         *
         * And if you set {@linkplain PermissionConfiguration.Builder#rationaleDialogProvider(DialogProvider)} then this
         * field will be ignored. Please make sure you handled in your custom dialogProvider implementation.
         */
        public Builder rationalMessage(String rationalMessage) {
            this.rationalMessage = rationalMessage;
            return this;
        }

        /**
         * If you need to ask any other permissions beside {@linkplain Defaults#LOCATION_PERMISSIONS}
         * or you may not need both of those permissions, you can change permissions
         * by calling this method with new permissions' array.
         */
        public Builder requiredPermissions(String[] permissions) {
            this.requiredPermissions = permissions;
            return this;
        }

        /**
         * If you need to display a custom dialog to display rationale to user, you can provide your own
         * implementation of {@linkplain DialogProvider} and manager will use that implementation to display the dialog.
         * Important, if you set your own implementation, please make sure to handle rationaleMessage as well.
         * Because {@linkplain PermissionConfiguration.Builder#rationalMessage} will be ignored in that case.
         *
         * If you don't specify any dialogProvider implementation {@linkplain SimpleMessageDialogProvider} will be used with
         * given {@linkplain PermissionConfiguration.Builder#rationalMessage}
         */
        public Builder rationaleDialogProvider(DialogProvider dialogProvider) {
            this.rationaleDialogProvider = dialogProvider;
            return this;
        }

        /**
         * If you already have a mechanism to handle runtime permissions, you can provide your own implementation of
         * {@linkplain PermissionProvider} and manager will use that implementation to ask required permissions.
         * Important, if you set your own implementation, please make sure to handle dialogProvider as well.
         * Because {@linkplain PermissionConfiguration.Builder#rationaleDialogProvider} will be ignored in that case.
         *
         * If you don't specify any permissionProvider implementation {@linkplain DefaultPermissionProvider} will be used
         * with given {@linkplain PermissionConfiguration.Builder#rationaleDialogProvider}
         */
        public Builder permissionProvider(PermissionProvider permissionProvider) {
            this.permissionProvider = permissionProvider;
            return this;
        }

        public PermissionConfiguration build() {
            if (requiredPermissions == null || requiredPermissions.length == 0) {
                throw new IllegalStateException("Required Permissions cannot be empty.");
            }

            if (rationaleDialogProvider == null && !TextUtils.isEmpty(rationalMessage)) {
                rationaleDialogProvider = new SimpleMessageDialogProvider(rationalMessage);
            }

            if (permissionProvider == null) {
                permissionProvider = new DefaultPermissionProvider(requiredPermissions, rationaleDialogProvider);
            }

            return new PermissionConfiguration(this);
        }
    }
}
