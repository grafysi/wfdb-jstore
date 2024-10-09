package com.grafysi.wfdb.driver.exception;

public class WfdbException extends Exception {

    public WfdbException(String message) {
        super(message);
    }

    public WfdbException(Throwable cause) {
        super(cause);
    }
}
