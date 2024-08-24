/*
package io.graphys.wfdbjstore.server.handler;

import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelPipeline;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.ReentrantLock;

public abstract class SingleStateHandlerFactory<T, R extends ChannelHandler> {
    private final SingleStateInstantiation<T,R> instantiation;
    private final Map<T,R> handlers = new HashMap<>();
    private final AtomicInteger numAppendingPipelines = new AtomicInteger(0);
    private final ReentrantLock lock = new ReentrantLock();

    public SingleStateHandlerFactory(SingleStateInstantiation<T,R> instantiation) {
        this.instantiation = instantiation;
    }

    public R getHandler(T arg) throws InstantiatingArgumentException {
        lock.lock();
        try {
            if (!handlers.containsKey(arg)) {
                var handler = instantiation.newInstance(arg);
                handlers.put(arg, handler);
                return handler;
            }
            return handlers.get(arg);
        } finally {
            lock.unlock();
        }
    }

    public void getHandlerAppendingPipeline(ChannelPipeline pipeline, T arg) throws InstantiatingArgumentException {
        var handler = getHandler(arg);
        pipeline.addLast(handler);
        numAppendingPipelines.incrementAndGet();
    }

    public int countInstances() {
        lock.lock();
        try {
            return handlers.size();
        } finally {
            lock.unlock();
        }
    }

    public int countAppendedPipelines() {
        return numAppendingPipelines.get();
    }
}
























*/
