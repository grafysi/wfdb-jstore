package io.graphys.wfdbjstore.recordstore.exception;

public class SignalDataPrepareFailedException extends RuntimeException {
    public SignalDataPrepareFailedException() {
        super();
    }

    public SignalDataPrepareFailedException(String msg) {
        super(msg);
    }

    public SignalDataPrepareFailedException(String msg, Throwable cause) {
        super(msg, cause);
    }
}
