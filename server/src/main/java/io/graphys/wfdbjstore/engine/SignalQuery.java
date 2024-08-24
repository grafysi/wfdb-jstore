package io.graphys.wfdbjstore.engine;

import io.graphys.wfdbjstore.recordstore.SignalInput;

public interface SignalQuery extends WfdbExecution {

    default SignalInput getSignalInput() throws ContextualAccessException {
        return ExecutionContext.getSignalInput();
    }
}
