package io.graphys.wfdbjstore.engine;

public class ExecutionNotAllowedException extends Exception {
    public ExecutionNotAllowedException() {
        super();
    }

    public ExecutionNotAllowedException(String msg) {
        super(msg);
    }

    public ExecutionNotAllowedException(Throwable cause) {
        super(cause);
    }

    public ExecutionNotAllowedException(String msg, Throwable cause) {
        super(msg, cause);
    }
}
