package io.graphys.wfdbjstore.recordstore.exception;

public class RecordConstructFailedException extends RuntimeException{
    public RecordConstructFailedException() {
        super();
    }

    public RecordConstructFailedException(String msg) {
        super(msg);
    }

    public RecordConstructFailedException(String msg, Throwable cause) {
        super(msg, cause);
    }
}
