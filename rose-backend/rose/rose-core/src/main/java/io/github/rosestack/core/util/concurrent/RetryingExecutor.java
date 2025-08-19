package io.github.rosestack.core.util.concurrent;

import java.time.Duration;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.Executor;
import java.util.concurrent.atomic.AtomicBoolean;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Simple {@link Executor} that executes the command continuously in a loop and implements a delayed retry logic
 * when the command throws an exception.
 */
public class RetryingExecutor implements Executor {
    private static final Logger LOGGER = LoggerFactory.getLogger(RetryingExecutor.class);
    private static final int DEFAULT_RETRY_DELAY = 10000;
    private final AtomicBoolean active = new AtomicBoolean(false);
    private final Timer timer = new Timer();
    private final String name;
    private volatile Duration retryDelay = Duration.ofMillis(DEFAULT_RETRY_DELAY);
    private volatile Thread thread;
    private volatile Runnable command;

    /**
     * Creates a {@link RetryingExecutor} with default parameters.
     */
    public RetryingExecutor() {
        this.name = "retry";
    }

    /**
     * Creates a {@link RetryingExecutor} with the specified name but otherwise default parameters.
     *
     * @param name the name of the executor (used for naming the executing thread).
     */
    public RetryingExecutor(String name) {
        this.name = name;
    }

    /**
     * Creates a {@link RetryingExecutor} with the specified name and the the specified retry delay.
     *
     * @param name       the name of the executor (used for naming the executing thread).
     * @param retryDelay the delay to wait before attempting a retry.
     */
    public RetryingExecutor(String name, Duration retryDelay) {
        this.name = name;
        this.retryDelay = retryDelay;
    }

    /**
     * @return the delay to wait before attempting a retry.
     */
    public Duration getRetryDelay() {
        return retryDelay;
    }

    /**
     * Sets the delay to way before attempting a retry.
     *
     * @param retryDelay the retry delay.
     */
    public void setRetryDelay(Duration retryDelay) {
        this.retryDelay = retryDelay;
    }

    /**
     * Starts the thread executing the command continuously.
     */
    public synchronized void start() {
        if (!active.getAndSet(true)) {
            startThread();
        }
    }

    /**
     * Stops the thread executing the command continuously, interrupting it if necessary.
     */
    public synchronized void stop() {
        if (active.getAndSet(false)) {
            timer.cancel();
            thread.interrupt();
        }
    }

    /**
     * Executes the specified command continuously if the executor is started.
     *
     * @param command the command to execute.
     */
    public synchronized void execute(Runnable command) {
        if (active.get()) {
            stop();
            this.command = command;
            start();
        } else {
            this.command = command;
        }
    }

    private void startThread() {
        thread = new Thread(() -> {
            LOGGER.debug("Retry executor {} is starting", getName());

            while (active.get()) {
                try {
                    command.run();
                } catch (Exception e) {
                    LOGGER.error("An exception occurred during {} command execution", getName(), e);
                    break;
                }
            }

            if (active.get()) {
                LOGGER.warn(
                        "Interruption of {} command execution. A retry is scheduled in {} ms",
                        getName(),
                        retryDelay.toMillis());
                try {
                    timer.schedule(new MyTimerTask(), retryDelay.toMillis());
                } catch (Exception e) {
                    LOGGER.error("Unable to schedule a retry of {} command execution", getName(), e);
                }
            } else {
                LOGGER.debug("Retry executor {} is stopping", getName());
            }
        });
        thread.setName(getName());
        thread.start();
    }

    private String getName() {
        return name + "-" + thread.getId();
    }

    private class MyTimerTask extends TimerTask {
        @Override
        public void run() {
            synchronized (RetryingExecutor.this) {
                if (!thread.isAlive()) {
                    startThread();
                } else {
                    timer.schedule(new MyTimerTask(), retryDelay.toMillis());
                }
            }
        }
    }
}
