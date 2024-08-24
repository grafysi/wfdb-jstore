package io.graphys.wfdbjstore.engine;

import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.locks.ReentrantLock;

public class SessionSafeRunner implements Runnable {

    private final Queue<WfdbExecution> executions = new LinkedList<>();

    private final AtomicBoolean running = new AtomicBoolean(false);

    private final ReentrantLock lock = new ReentrantLock();

    @Override
    public void run() {
        if (running.compareAndSet(false, true)) {
            while (shouldContinue()) {
                var execution = executions.poll();
                Objects.requireNonNull(execution);
                execution.execute();
            }
        }
    }

    public void addExecution(WfdbExecution execution) {
        Objects.requireNonNull(execution);
        try {
            lock.lock();
            executions.offer(execution);
        } finally {
            lock.unlock();
        }
    }

    public boolean isRunning() {
        return running.get();
    }

    private boolean shouldContinue() {
        try {
            lock.lock();
            if (executions.isEmpty()) {
                running.set(false);
                return false;
            }
            return true;
        } finally {
            lock.unlock();
        }
    }

}


















