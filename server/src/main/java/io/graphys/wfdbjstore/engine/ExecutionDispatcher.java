package io.graphys.wfdbjstore.engine;

import io.graphys.wfdbjstore.server.SessionValidationException;

public interface ExecutionDispatcher {
    public void dispatch(WfdbExecution execution, String sessionId)
            throws ExecutionNotAllowedException, SessionValidationException;
}
