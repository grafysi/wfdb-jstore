package io.graphys.wfdbjstore.recordstore.exception;

public class ReadingInterruptedException extends RuntimeException {
    public ReadingInterruptedException() {
        super();
    }

    public ReadingInterruptedException(String msg) {
        super(msg);
    }

    public ReadingInterruptedException(String msg, Throwable throwable) {
        super(msg, throwable);
    }
}
