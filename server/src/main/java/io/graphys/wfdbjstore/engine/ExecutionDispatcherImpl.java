package io.graphys.wfdbjstore.engine;

import io.graphys.wfdbjstore.server.SessionValidationException;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

public class ExecutionDispatcherImpl implements ExecutionDispatcher {
    private final List<WfdbExecutor> executors;

    public ExecutionDispatcherImpl(WfdbExecutor... executors) {
        this.executors = new LinkedList<>();
        this.executors.addAll(Arrays.asList(executors));
    }

    @Override
    public void dispatch(WfdbExecution execution, String sessionId) throws ExecutionNotAllowedException, SessionValidationException {
        var matchedExecutor = executors
                .stream()
                .filter(e -> e.isAllowedExecution(execution))
                .findFirst()
                .orElse(null);

        if (matchedExecutor == null) {
            throw new ExecutionNotAllowedException("No executor found for this execution.");
        }

        if (sessionId == null && matchedExecutor instanceof SessionBasedExecutor<?,?> ) {
            throw new ExecutionNotAllowedException("Session required for this execution but be null!");
        }

        switch (matchedExecutor) {
            case MetadataQueryExecutor e: e.submit((MetadataQuery<?>) execution, sessionId); break;
            case SessionInitializationExecutor e: e.submit((SessionInitialization<?>) execution); break;
            case SessionSafeExecutor e: e.submit(execution, sessionId); break;
            default: throw new IllegalStateException("Executor matched expected");
        }
    }
}



























