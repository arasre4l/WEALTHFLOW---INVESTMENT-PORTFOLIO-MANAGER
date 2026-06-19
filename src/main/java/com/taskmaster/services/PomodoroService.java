package com.taskmaster.services;

import com.taskmaster.models.Task;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.util.Duration;

public class PomodoroService {
    private static final int WORK_DURATION = 25 * 60;
    private static final int SHORT_BREAK = 5 * 60;
    private static final int LONG_BREAK = 15 * 60;

    private final IntegerProperty remainingSeconds;
    private Timeline timeline;
    private Task currentTask;
    private boolean isRunning;
    private PomodoroPhase currentPhase;

    public enum PomodoroPhase {
        WORK, SHORT_BREAK, LONG_BREAK
    }

    public PomodoroService() {
        this.remainingSeconds = new SimpleIntegerProperty(WORK_DURATION);
        this.currentPhase = PomodoroPhase.WORK;
        this.isRunning = false;
    }

    public void startPomodoro(Task task) {
        this.currentTask = task;
        this.currentPhase = PomodoroPhase.WORK;
        this.remainingSeconds.set(WORK_DURATION);
        startTimer();
    }

    public void startBreak(boolean isLongBreak) {
        this.currentPhase = isLongBreak ? PomodoroPhase.LONG_BREAK : PomodoroPhase.SHORT_BREAK;
        this.remainingSeconds.set(isLongBreak ? LONG_BREAK : SHORT_BREAK);
        startTimer();
    }

    private void startTimer() {
        if (timeline != null) {
            timeline.stop();
        }

        timeline = new Timeline(new KeyFrame(Duration.seconds(1), event -> {
            int current = remainingSeconds.get();
            if (current > 0) {
                remainingSeconds.set(current - 1);
            } else {
                handleTimerComplete();
            }
        }));
        
        timeline.setCycleCount(Timeline.INDEFINITE);
        timeline.play();
        isRunning = true;
    }

    private void handleTimerComplete() {
        stop();
        if (currentPhase == PomodoroPhase.WORK && currentTask != null) {
            currentTask.incrementPomodoro();
        }
    }

    public void pause() {
        if (timeline != null) {
            timeline.pause();
            isRunning = false;
        }
    }

    public void resume() {
        if (timeline != null) {
            timeline.play();
            isRunning = true;
        }
    }

    public void stop() {
        if (timeline != null) {
            timeline.stop();
            timeline = null;
        }
        isRunning = false;
    }

    public void reset() {
        stop();
        remainingSeconds.set(WORK_DURATION);
        currentPhase = PomodoroPhase.WORK;
    }

    public IntegerProperty remainingSecondsProperty() {
        return remainingSeconds;
    }

    public String getFormattedTime() {
        int seconds = remainingSeconds.get();
        int minutes = seconds / 60;
        int secs = seconds % 60;
        return String.format("%02d:%02d", minutes, secs);
    }

    public boolean isRunning() {
        return isRunning;
    }

    public PomodoroPhase getCurrentPhase() {
        return currentPhase;
    }

    public Task getCurrentTask() {
        return currentTask;
    }
}
