package io.graphys.wfdbjstore.engine.session;

public class UnsupportedDatabaseException extends Exception {
    public UnsupportedDatabaseException() {
        super();
    }

    public UnsupportedDatabaseException(String msg) {
        super(msg);
    }

    public UnsupportedDatabaseException(String msg, Throwable cause) {
        super(msg, cause);
    }

    public UnsupportedDatabaseException(Throwable cause) {
        super(cause);
    }
}
