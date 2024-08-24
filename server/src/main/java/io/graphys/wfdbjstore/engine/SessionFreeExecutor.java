package io.graphys.wfdbjstore.engine;

public abstract class SessionFreeExecutor<T extends WfdbExecution> implements WfdbExecutor {
    public abstract void submit(T execution);
}
