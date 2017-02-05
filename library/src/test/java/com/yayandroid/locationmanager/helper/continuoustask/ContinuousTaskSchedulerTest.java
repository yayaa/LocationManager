package com.yayandroid.locationmanager.helper.continuoustask;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

public class ContinuousTaskSchedulerTest {
    private static final long INITIAL_TIME = 10000L;
    private static final long DELAY = 1000L;
    private static final long DURATION = 50L;

    @Mock ContinuousTask continuousTask;
    private ContinuousTaskScheduler continuousTaskScheduler;

    @Before
    public void setUp() throws Exception {
        MockitoAnnotations.initMocks(this);

        continuousTaskScheduler = new ContinuousTaskScheduler(continuousTask);

        when(continuousTask.getCurrentTime()).thenReturn(INITIAL_TIME);
    }

    @Test
    public void whenDelayedNotCalledIsSetShouldReturnFalse() throws Exception {
        assertThat(continuousTaskScheduler.isSet()).isFalse();
    }

    @Test
    public void whenDelayedCalledIsSetShouldReturnTrue() throws Exception {
        continuousTaskScheduler.delayed(0);
        assertThat(continuousTaskScheduler.isSet()).isTrue();
    }

    @Test
    public void whenOnPauseCalledIsSetShouldReturnFalse() throws Exception {
        continuousTaskScheduler.delayed(0);
        continuousTaskScheduler.onPause();
        assertThat(continuousTaskScheduler.isSet()).isFalse();
    }

    @Test
    public void whenOnResumeCalledIsSetShouldReturnTrue() throws Exception {
        continuousTaskScheduler.delayed(0);
        continuousTaskScheduler.onResume();
        assertThat(continuousTaskScheduler.isSet()).isTrue();
    }

    @Test
    public void whenOnStopCalledIsSetShouldReturnFalse() throws Exception {
        continuousTaskScheduler.delayed(0);
        continuousTaskScheduler.onStop();
        assertThat(continuousTaskScheduler.isSet()).isFalse();
    }

    @Test
    public void whenCleanCalledIsSetShouldReturnFalse() throws Exception {
        continuousTaskScheduler.delayed(0);
        continuousTaskScheduler.clean();
        assertThat(continuousTaskScheduler.isSet()).isFalse();
    }

    @Test
    public void whenDelayedNotCalledTaskShouldHaveNoInteractionOnPauseAndResume() throws Exception {
        continuousTaskScheduler.onPause();
        verify(continuousTask, never()).unregister();

        continuousTaskScheduler.set(0);
        verify(continuousTask, never()).delayed(0);
    }

    @Test
    public void whenDelayedCalledTaskShouldSchedule() throws Exception {
        continuousTaskScheduler.delayed(DELAY);
        verify(continuousTask).schedule(DELAY);
    }

    @Test
    public void whenOnPauseCalledTaskShouldUnregister() throws Exception {
        continuousTaskScheduler.delayed(DELAY);
        continuousTaskScheduler.onPause();
        verify(continuousTask).unregister();
    }

    @Test
    public void whenOnResumeCalledTaskShouldReScheduled() throws Exception {
        continuousTaskScheduler.delayed(DELAY);
        verify(continuousTask).schedule(DELAY);

        when(continuousTask.getCurrentTime()).thenReturn(INITIAL_TIME + DURATION);
        continuousTaskScheduler.onPause();
        verify(continuousTask).unregister();

        continuousTaskScheduler.onResume();
        verify(continuousTask).schedule(DELAY - DURATION);
    }

    @Test
    public void whenOnStopCalledTaskShouldHaveNoInteractionOnPauseAndResume() throws Exception {
        continuousTaskScheduler.delayed(0);
        verify(continuousTask).getCurrentTime();
        verify(continuousTask).schedule(0);

        continuousTaskScheduler.onStop();
        verify(continuousTask).unregister();

        continuousTaskScheduler.onPause();
        continuousTaskScheduler.onResume();
        verifyNoMoreInteractions(continuousTask);
    }

    @Test
    public void whenCleanCalledTaskShouldHaveNoInteractionOnPauseAndResume() throws Exception {
        continuousTaskScheduler.delayed(0);
        verify(continuousTask).getCurrentTime();
        verify(continuousTask).schedule(0);

        continuousTaskScheduler.clean();

        continuousTaskScheduler.onPause();
        continuousTaskScheduler.onResume();
        verifyNoMoreInteractions(continuousTask);
    }

    @Test
    public void whenTaskIsAlreadyScheduledOnResumeShouldHaveNoInteraction() throws Exception {
        continuousTaskScheduler.delayed(0);
        verify(continuousTask).getCurrentTime();
        verify(continuousTask).schedule(0);

        continuousTaskScheduler.onResume();
        verifyNoMoreInteractions(continuousTask);
    }

}