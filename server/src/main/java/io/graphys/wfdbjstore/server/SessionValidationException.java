package io.graphys.wfdbjstore.server;

public class SessionValidationException extends Exception{
    public SessionValidationException() {
        super();
    }

    public SessionValidationException(String msg) {
        super(msg);
    }

    public SessionValidationException(Throwable cause) {
        super(cause);
    }

    public SessionValidationException(String msg, Throwable cause) {
        super(msg, cause);
    }
}
