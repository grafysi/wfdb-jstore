package io.graphys.wfdbjstore.protocore.report;

public class ReadReportException extends RuntimeException {
    public ReadReportException() {
        super();
    }

    public ReadReportException(String msg) {
        super(msg);
    }

    public ReadReportException(Throwable cause) {
        super(cause);
    }

    public ReadReportException(String msg, Throwable cause) {
        super(msg, cause);
    }
}
