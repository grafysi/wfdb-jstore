package io.graphys.wfdbjstore.protocore.report;

public class WriteReportException extends Exception {
    public WriteReportException() {
        super();
    }

    public WriteReportException(String msg) {
        super(msg);
    }

    public WriteReportException(Throwable cause) {
        super(cause);
    }

    public WriteReportException(String msg, Throwable cause) {
        super(msg, cause);
    }
}
