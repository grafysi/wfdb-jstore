package io.graphys.wfdbjstore.server;

public class IllegalCommandException extends Exception {
    public IllegalCommandException() {
        super();
    }

    public IllegalCommandException(String msg) {
        super(msg);
    }

    public IllegalCommandException(Throwable cause) {
        super(cause);
    }

    public IllegalCommandException(String msg, Throwable cause) {
        super(msg, cause);
    }
}
