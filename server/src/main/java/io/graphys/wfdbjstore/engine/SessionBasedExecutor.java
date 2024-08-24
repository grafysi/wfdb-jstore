package io.graphys.wfdbjstore.engine;

import io.graphys.wfdbjstore.server.SessionValidationException;
import io.graphys.wfdbjstore.engine.session.Session;
import io.graphys.wfdbjstore.engine.session.SessionManager;

public abstract class SessionBasedExecutor<T extends WfdbExecution, S extends Session> implements WfdbExecutor {
    private final SessionManager<S> sessionManager;

    public SessionBasedExecutor(SessionManager<S> sessionManager) {
        this.sessionManager = sessionManager;
    }

    public final void submit(T execution, String sessionId) throws SessionValidationException {
        var session = sessionManager.getSession(sessionId);
        if (session == null) {
            throw new SessionValidationException("No session found for given id: " + sessionId);
        }
        submit(execution, session);
    }

    protected abstract void submit(T execution, S session);
}
