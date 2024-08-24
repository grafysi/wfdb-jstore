package io.graphys.wfdbjstore.engine;

import io.graphys.wfdbjstore.engine.session.SessionManager;
import io.graphys.wfdbjstore.engine.session.SessionRegistry;
import io.graphys.wfdbjstore.engine.session.auth.AuthToken;
import io.graphys.wfdbjstore.engine.session.auth.AuthenticationUtils;
import io.graphys.wfdbjstore.engine.session.auth.UnsupportedSchemeException;
import io.graphys.wfdbjstore.engine.session.Session;

public interface SessionInitialization<T extends Session> extends WfdbExecution {

    default AuthToken getAuthToken(String scheme, String token) throws UnsupportedSchemeException {
        return AuthenticationUtils.newAuthToken(scheme, token);
    }

    default SessionRegistry<T> getSessionRegistry() throws ContextualAccessException {
        return ExecutionContext.getSessionRegistry(getSessionClass());
    }

    public Class<T> getSessionClass();
}
