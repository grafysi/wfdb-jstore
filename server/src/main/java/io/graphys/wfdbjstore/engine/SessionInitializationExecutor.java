package io.graphys.wfdbjstore.engine;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class SessionInitializationExecutor extends SessionFreeExecutor<SessionInitialization<?>> {
    private final ExecutionContext executionContext;

    private final ExecutorService executorService = Executors.newVirtualThreadPerTaskExecutor();

    public SessionInitializationExecutor(ExecutionContext context) {
        this.executionContext = context;
    }

    @Override
    public void submit(SessionInitialization<?> initialization) {
        executionContext.startSessionInitializationContext(initialization::execute);
        //executorService.submit(() -> executionContext.startSessionInitializationContext(initialization::execute));
        //initialization.execute();
    }

    @Override
    public Class<? extends WfdbExecution> getExecutionClass() {
        return SessionInitialization.class;
    }
}
