package io.graphys.wfdbjstore.engine.session;

public class RecordNotFoundException extends Exception {
    public RecordNotFoundException() {
        super();
    }

    public RecordNotFoundException(String msg) {
        super(msg);
    }

    public RecordNotFoundException(Throwable cause) {
        super(cause);
    }

    public RecordNotFoundException(String msg, Throwable cause) {
        super(msg, cause);
    }
}
