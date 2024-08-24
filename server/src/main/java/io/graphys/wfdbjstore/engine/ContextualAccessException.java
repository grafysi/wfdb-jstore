package io.graphys.wfdbjstore.engine;

public class ContextualAccessException extends Exception {
    public ContextualAccessException() {
        super();
    }

    public ContextualAccessException(String msg) {
        super(msg);
    }

    public ContextualAccessException(Throwable cause) {
        super(cause);
    }

    public ContextualAccessException(String msg, Throwable cause) {
        super(msg, cause);
    }
}
