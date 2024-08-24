package io.graphys.wfdbjstore.engine;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.Closeable;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Queue;
import java.util.concurrent.StructuredTaskScope;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;

public class MetadataQueryCacheRunner implements Closeable, Runnable {

    private static final Logger logger = LogManager.getLogger(MetadataQueryCacheRunner.class);

    private final Queue<MetadataQuery<?>> queryQueue = new LinkedList<>();

    private final HashMap<MetadataQuery<?>, MetadataQuery<?>> queryCaches = new HashMap<>();

    private boolean stopped = false;

    private final ReentrantLock lock = new ReentrantLock();

    private final Condition queryQueueNotEmpty = lock.newCondition();


    @Override
    public void close() {
        stop();
    }

    @Override
    public void run() {
        if (isStopped()) {
            throw new IllegalStateException("This runner has been stopped");
        }
        try (var scope = new StructuredTaskScope<>()) {
            while (!isStopped()) {
                var query = takeQuery();
                if (!isStopped() && !tryCaching(query)) {
                    queryCaches.put(query, query);
                    scope.fork(() -> {
                        query.execute();
                        return null;
                    });
                }
            }
        }
    }

    private boolean tryCaching(MetadataQuery<?> query) {
        if (queryCaches.containsKey(query) && query.getResultConsumer() != null) {
            return queryCaches.get(query).shareResult(query.getResultConsumer());
        }
        return false;
    }

    public boolean isStopped() {
        lock.lock();
        try {
            return stopped;
        } finally {
            lock.unlock();
        }
    }

    private void stop() {
        lock.lock();
        try {
            stopped = true;
        } finally {
            lock.unlock();
        }
    }

    private MetadataQuery<?> takeQuery() {
        lock.lock();
        try {
            while (!stopped && queryQueue.isEmpty()) {
                queryQueueNotEmpty.await();
            }
            return queryQueue.poll();
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        } finally {
            lock.unlock();
        }
    }

    public void putQuery(MetadataQuery<?> query) {
        lock.lock();
        try {
            queryQueue.offer(query);
        } finally {
            queryQueueNotEmpty.signal();
            lock.unlock();
        }
    }
}





















