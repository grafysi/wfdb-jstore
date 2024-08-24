package io.graphys.wfdbjstore.engine.session.auth;

public class UnsupportedSchemeException extends AuthenticationException {
    public UnsupportedSchemeException() {
        super();
    }

    public UnsupportedSchemeException(String msg) {
        super(msg);
    }

    public UnsupportedSchemeException(Throwable cause) {
        super(cause);
    }

    public UnsupportedSchemeException(String msg, Throwable cause) {
        super(msg, cause);
    }
}
