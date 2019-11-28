package com.rd162.eventbus;

public interface RunnableEventConsumer extends Runnable {
    boolean isRunning();

    boolean isDone();

    Throwable getException();

    void start();

    void shutdown();
}
