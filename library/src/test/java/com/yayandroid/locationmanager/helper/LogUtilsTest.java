package com.yayandroid.locationmanager.helper;

import com.yayandroid.locationmanager.helper.logging.Logger;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyZeroInteractions;
import static org.mockito.internal.verification.VerificationModeFactory.times;

public class LogUtilsTest {

    @Mock
    private Logger mockLogger;

    @Before
    public void setUp() {
        MockitoAnnotations.initMocks(this);
        LogUtils.setLogger(mockLogger);
    }

    @Test
    public void whenLoggingIsDisabledItShouldNotForwardToLogger() {
        LogUtils.enable(false);

        LogUtils.logD("Dmessage");
        LogUtils.logE("Emessage");
        LogUtils.logI("Imessage");
        LogUtils.logV("Vmessage");
        LogUtils.logW("Wmessage");

        verifyZeroInteractions(mockLogger);
    }

    @Test
    public void whenLoggingIsEnabledItShouldForwardToLogger() {
        LogUtils.enable(true);

        LogUtils.logD("Dmessage");
        LogUtils.logE("Emessage");
        LogUtils.logI("Imessage");
        LogUtils.logV("Vmessage");
        LogUtils.logW("Wmessage");

        verify(mockLogger, times(1)).logD(anyString(), eq("Dmessage"));
        verify(mockLogger, times(1)).logE(anyString(), eq("Emessage"));
        verify(mockLogger, times(1)).logI(anyString(), eq("Imessage"));
        verify(mockLogger, times(1)).logV(anyString(), eq("Vmessage"));
        verify(mockLogger, times(1)).logW(anyString(), eq("Wmessage"));
    }

    @Test
    public void whenChangingLoggerItShouldLogIntoIt() {
        LogUtils.enable(true);

        Logger newLogger = mock(Logger.class);
        LogUtils.setLogger(newLogger);
        LogUtils.logD("Dmessage");

        verify(newLogger, times(1)).logD(anyString(), eq("Dmessage"));
        verify(mockLogger, times(0)).logD(anyString(), anyString());
    }
}
