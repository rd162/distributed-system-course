package com.rd162.eventbus;

import com.rd162.eventbus.ConsumerConfiguration;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.FutureTask;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;

public abstract class AbstractRunnableEventConsumer implements RunnableEventConsumer {

    protected enum State {
        Clean,
        Running,
        Completed
    }

    static private final String THREAD_GROUP_NAME_BASE = "event-consumer-thread-group";
    static private final ConcurrentHashMap<String, ThreadGroup> threadGroups = new ConcurrentHashMap<String, ThreadGroup>();

    private final String consumerGroup;
    private State state = State.Clean;
    private Thread thread = null;
    private FutureTask<Object> task = null;
    private Throwable exception = null;
    private final ReentrantLock lock = new ReentrantLock();
    private final Condition running = lock.newCondition();

    protected AbstractRunnableEventConsumer(ConsumerConfiguration configuration) {
        if (configuration == null) {
            throw new IllegalArgumentException("configuration cannot be null");
        }
        this.consumerGroup = configuration.getConsumerGroup();
    }

    private static String GetThreadGroupName(String consumerGroup) {
        return THREAD_GROUP_NAME_BASE + " | " + consumerGroup;
    }

    private ThreadGroup GetThreadGroup() {
        String threadGroupName = GetThreadGroupName(consumerGroup);
        ThreadGroup threadGroup = threadGroups.get(threadGroupName);
        if (threadGroup == null || threadGroup.isDestroyed()) {
            threadGroup = new ThreadGroup(threadGroupName);
            threadGroup.setDaemon(true);
            threadGroups.put(threadGroupName, threadGroup);
        }
        return threadGroup;
    }

    protected void setState(State state) {
        lock.lock();
        try {
            this.state = state;
            if (state == State.Running) {
                running.signalAll();
            }
        } finally {
            lock.unlock();
        }
    }

    @Override
    public boolean isRunning() {
        lock.lock();
        try {
            return state == State.Running && (task == null || !task.isCancelled());
        } finally {
            lock.unlock();
        }
    }

    @Override
    public boolean isDone() {
        lock.lock();
        try {
            return state == State.Completed && (task == null || task.isDone());
        } finally {
            lock.unlock();
        }
    }

    @Override
    public Throwable getException() {
        lock.lock();
        try {
            if (exception != null) {
                return exception;
            }
            if (!isDone()) {
                return null;
            }
            if (task != null) {
                try {
                    task.get();
                } catch (Throwable t) {
                    exception = t;
                }
            }
            return exception;
        } finally {
            lock.unlock();
        }
    }

    protected void clear() {
        lock.lock();
        try {
            if (isRunning()) {
                throw new IllegalStateException("Cannot clear consumer when it is running");
            }
            task = null;
            thread = null;
            exception = null;
            state = State.Clean;
        } finally {
            lock.unlock();
        }
    }

    @Override
    public void start() {
        lock.lock();
        try {
            if (isRunning()) {
                throw new IllegalStateException("The consumer is already running");
            }
            clear();
            task = new FutureTask<>(this, null);
            thread = new Thread(GetThreadGroup(), task, "event-consumer-thread");
            thread.setDaemon(true);
            thread.start();
            running.await();
        } catch (InterruptedException e) {
            return;
        } finally {
            lock.unlock();
        }
    }

    @Override
    public void shutdown() {
        lock.lock();
        try {
            if (isDone()) {
                return;
            }
            if (task != null) {
                task.cancel(false);
            }
            setState(State.Completed);
            Thread thread = this.thread;
            if (thread != null && Thread.currentThread() != thread) {
                lock.unlock();
                try {
                    thread.join();
                } catch (InterruptedException e) {
                    return;
                } finally {
                    lock.lock();
                }
            }
        } finally {
            lock.unlock();
        }
    }
}
