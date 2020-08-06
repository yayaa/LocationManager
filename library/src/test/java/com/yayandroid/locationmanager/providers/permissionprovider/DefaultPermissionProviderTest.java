package com.yayandroid.locationmanager.providers.permissionprovider;

import android.app.Activity;
import android.content.pm.PackageManager;

import androidx.fragment.app.Fragment;

import com.yayandroid.locationmanager.constants.RequestCode;
import com.yayandroid.locationmanager.listener.PermissionListener;
import com.yayandroid.locationmanager.fakes.MockDialogProvider;
import com.yayandroid.locationmanager.view.ContextProcessor;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyZeroInteractions;
import static org.mockito.Mockito.when;

public class DefaultPermissionProviderTest {

    private static final String[] REQUIRED_PERMISSIONS = new String[]{"really_important_permission",
          "even_more_important", "super_important_one"};
    private static final String SINGLE_PERMISSION = REQUIRED_PERMISSIONS[0];
    private static final int GRANTED = PackageManager.PERMISSION_GRANTED;
    private static final int DENIED = PackageManager.PERMISSION_DENIED;

    @Rule public ExpectedException expectedException = ExpectedException.none();

    @Mock Fragment fragment;
    @Mock Activity activity;
    @Mock ContextProcessor contextProcessor;
    @Mock PermissionListener permissionListener;
    @Mock PermissionCompatSource permissionCompatSource;

    private DefaultPermissionProvider defaultPermissionProvider;
    private MockDialogProvider mockDialogProvider;

    @Before
    public void setUp() throws Exception {
        MockitoAnnotations.initMocks(this);

        mockDialogProvider = new MockDialogProvider("");
        defaultPermissionProvider = new DefaultPermissionProvider(REQUIRED_PERMISSIONS, mockDialogProvider);
        defaultPermissionProvider.setContextProcessor(contextProcessor);
        defaultPermissionProvider.setPermissionListener(permissionListener);
        defaultPermissionProvider.setPermissionCompatSource(permissionCompatSource);
    }

    @Test
    public void executePermissionsRequestShouldNotifyDeniedWhenThereIsNoActivityOrFragment() {
        defaultPermissionProvider.executePermissionsRequest();
        verify(permissionListener).onPermissionsDenied();
    }

    @Test
    public void executePermissionsRequestShouldCallRequestPermissionsOnFragmentFirst() {
        when(contextProcessor.getActivity()).thenReturn(activity);
        when(contextProcessor.getFragment()).thenReturn(fragment);

        defaultPermissionProvider.executePermissionsRequest();

        verify(permissionCompatSource)
              .requestPermissions(eq(fragment), eq(REQUIRED_PERMISSIONS), eq(RequestCode.RUNTIME_PERMISSION));
    }

    @Test
    public void executePermissionsRequestShouldCallRequestPermissionsOnActivityIfThereIsNoFragment() {
        when(contextProcessor.getActivity()).thenReturn(activity);

        defaultPermissionProvider.executePermissionsRequest();

        verifyRequestPermissionOnActivity();
    }

    @Test
    public void checkRationaleForPermissionShouldReturnFalseIfThereIsNoActivityOrFragment() {
        assertThat(defaultPermissionProvider.checkRationaleForPermission(SINGLE_PERMISSION)).isFalse();
    }

    @Test
    public void checkRationaleForPermissionShouldCheckOnFragmentFirst() {
        when(contextProcessor.getActivity()).thenReturn(activity);
        when(contextProcessor.getFragment()).thenReturn(fragment);

        defaultPermissionProvider.checkRationaleForPermission(SINGLE_PERMISSION);

        verify(permissionCompatSource).shouldShowRequestPermissionRationale(eq(fragment), eq(SINGLE_PERMISSION));
    }

    @Test
    public void checkRationaleForPermissionShouldCheckOnActivityIfThereIsNoFragment() {
        when(contextProcessor.getActivity()).thenReturn(activity);

        defaultPermissionProvider.checkRationaleForPermission(SINGLE_PERMISSION);

        verify(permissionCompatSource).shouldShowRequestPermissionRationale(eq(activity), eq(SINGLE_PERMISSION));
    }

    @Test
    public void shouldShowRequestPermissionRationaleShouldReturnTrueWhenAnyIsTrue() {
        when(contextProcessor.getActivity()).thenReturn(activity);
        when(permissionCompatSource.shouldShowRequestPermissionRationale(eq(activity), eq(REQUIRED_PERMISSIONS[0])))
              .thenReturn(true);
        when(permissionCompatSource.shouldShowRequestPermissionRationale(eq(activity), eq(REQUIRED_PERMISSIONS[1])))
              .thenReturn(false);
        when(permissionCompatSource.shouldShowRequestPermissionRationale(eq(activity), eq(REQUIRED_PERMISSIONS[2])))
              .thenReturn(false);

        assertThat(defaultPermissionProvider.shouldShowRequestPermissionRationale()).isTrue();
    }

    @Test
    public void shouldShowRequestPermissionRationaleShouldReturnFalseWhenThereIsNoActivity() {
        when(contextProcessor.getActivity()).thenReturn(null);
        assertThat(defaultPermissionProvider.shouldShowRequestPermissionRationale()).isFalse();
    }

    @Test
    public void shouldShowRequestPermissionRationaleShouldReturnFalseWhenThereIsNoDialogProvider() {
        defaultPermissionProvider = new DefaultPermissionProvider(REQUIRED_PERMISSIONS, null);
        defaultPermissionProvider.setContextProcessor(contextProcessor);
        defaultPermissionProvider.setPermissionListener(permissionListener);
        defaultPermissionProvider.setPermissionCompatSource(permissionCompatSource);
        makeShouldShowRequestPermissionRationaleTrue();

        assertThat(defaultPermissionProvider.shouldShowRequestPermissionRationale()).isFalse();
    }

    @Test
    public void requestPermissionsShouldReturnFalseWhenThereIsNoActivity() {
        assertThat(defaultPermissionProvider.requestPermissions()).isFalse();
    }

    @Test
    public void requestPermissionsShouldRequestWhenShouldShowRequestPermissionRationaleIsFalse() {
        when(contextProcessor.getActivity()).thenReturn(activity);

        defaultPermissionProvider.requestPermissions();

        verifyRequestPermissionOnActivity();
    }

    @Test
    public void requestPermissionsShouldShowRationaleIfRequired() {
        makeShouldShowRequestPermissionRationaleTrue();

        defaultPermissionProvider.requestPermissions();

        verify(mockDialogProvider.getDialog(activity)).show();
    }

    @Test
    public void onPositiveButtonClickShouldRequestPermission() {
        when(contextProcessor.getActivity()).thenReturn(activity);

        defaultPermissionProvider.onPositiveButtonClick();

        verifyRequestPermissionOnActivity();
    }

    @Test
    public void onNegativeButtonClickShouldNotifyPermissionDenied() {
        defaultPermissionProvider.onNegativeButtonClick();
        verify(permissionListener).onPermissionsDenied();
    }

    @Test
    public void onRequestPermissionsResultShouldDoNothingWhenRequestCodeIsNotMatched() {
        defaultPermissionProvider.onRequestPermissionsResult(-1, null, new int[] {1});
        verifyZeroInteractions(permissionListener);
    }

    @Test
    public void onRequestPermissionsResultShouldNotifyDeniedIfAny() {
        defaultPermissionProvider.onRequestPermissionsResult(RequestCode.RUNTIME_PERMISSION,
              REQUIRED_PERMISSIONS, new int[] {GRANTED, GRANTED, DENIED});
        verify(permissionListener).onPermissionsDenied();
    }

    @Test
    public void onRequestPermissionsResultShouldNotifyGrantedIfAll() {
        defaultPermissionProvider.onRequestPermissionsResult(RequestCode.RUNTIME_PERMISSION,
              REQUIRED_PERMISSIONS, new int[] {GRANTED, GRANTED, GRANTED});
        verify(permissionListener).onPermissionsGranted();
    }

    private void makeShouldShowRequestPermissionRationaleTrue() {
        when(contextProcessor.getActivity()).thenReturn(activity);
        when(permissionCompatSource.shouldShowRequestPermissionRationale(eq(activity), eq(REQUIRED_PERMISSIONS[0])))
              .thenReturn(true);
    }

    private void verifyRequestPermissionOnActivity() {
        verify(permissionCompatSource)
              .requestPermissions(eq(activity), eq(REQUIRED_PERMISSIONS), eq(RequestCode.RUNTIME_PERMISSION));
    }

}