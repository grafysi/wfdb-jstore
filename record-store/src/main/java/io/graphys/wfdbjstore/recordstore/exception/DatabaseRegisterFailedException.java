package io.graphys.wfdbjstore.recordstore.exception;

public class DatabaseRegisterFailedException extends RuntimeException {
    public DatabaseRegisterFailedException() {
        super();
    }

    public DatabaseRegisterFailedException(String msg) {
        super(msg);
    }

    public DatabaseRegisterFailedException(String msg, Throwable cause) {
        super(msg, cause);
    }
}
