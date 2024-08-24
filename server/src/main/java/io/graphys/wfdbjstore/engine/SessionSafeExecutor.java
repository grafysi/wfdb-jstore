package io.graphys.wfdbjstore.engine;

import io.graphys.wfdbjstore.protocore.command.ReadSignalFlowCommand;

import java.io.Closeable;
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class SessionSafeExecutor implements WfdbExecutor, Closeable {

    private final ExecutorService executorService = Executors.newVirtualThreadPerTaskExecutor();

    private final ExecutionContext context;

    private final Map<String, SessionSafeRunner> sessionRunners = Collections.synchronizedMap(new HashMap<>());

    public SessionSafeExecutor(ExecutionContext ctx) {
        this.context = ctx;
    }

    @Override
    public boolean isAllowedExecution(WfdbExecution execution) {
        return (execution instanceof ReadSignalFlowCommand);
    }

    @Override
    public Class<? extends WfdbExecution> getExecutionClass() {
        // ignore for this implementation
        return null;
    }

    public void submit(WfdbExecution execution, String sessionId) {
        var runner = sessionRunners.computeIfAbsent(sessionId, sId -> new SessionSafeRunner());

        runner.addExecution(execution);

        if (!runner.isRunning()) {
            context.startSessionContext(
                    sessionId, ExecutionContext.SIGNAL_INPUT_DEPENDENCY, runner);
        }
    }

    public void close() {
        executorService.shutdown();
        executorService.close();
    }
}






























