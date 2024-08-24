package io.graphys.wfdbjstore.engine.session;

import io.graphys.wfdbjstore.engine.session.auth.AuthToken;
import io.graphys.wfdbjstore.engine.session.auth.AuthenticationException;

public interface SessionRegistry<T> {
    public T register(AuthToken token, T session)
            throws AuthenticationException, UnsupportedDatabaseException, RecordNotFoundException;
}
