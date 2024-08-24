package io.graphys.wfdbjstore.engine;

public class QueryMismatchException extends Exception {
    public QueryMismatchException() {
        super();
    }

    public QueryMismatchException(String msg) {
        super(msg);
    }

    public QueryMismatchException(Throwable cause) {
        super(cause);
    }

    public QueryMismatchException(String msg, Throwable cause) {
        super(msg, cause);
    }
}
