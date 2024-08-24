package io.graphys.wfdbjstore.protocore.report;

import io.graphys.wfdbjstore.protocol.exchange.CommandType;
import io.graphys.wfdbjstore.protocol.content.Content;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.*;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;

public class Report<T extends Content> {

    public enum Status {NOT_COMPLETED, SUCCESS, FAILED}
    private final CommandType commandType;
    private final List<T> contentList = new LinkedList<>();
    private final ListIterator<T> contentIterator = contentList.listIterator();
    private Status status = Status.NOT_COMPLETED;
    private Throwable failedCause = null;
    private String failedMessage = null;
    private final ReentrantLock reportLock = new ReentrantLock();
    private final Condition reportCompleted = reportLock.newCondition();
    private final ReportConsumer consumer;

    private final Reader reader;
    private final Writer writer;


    public Report(CommandType commandType, ReportConsumer consumer) {
        this.commandType = commandType;
        this.reader = new Reader();
        this.writer = new Writer();
        this.consumer = consumer;
    }

    public Writer getWriter() {
        return writer;
    }

    public Reader awaitGetReader() throws InterruptedException {
        while (isAtStatus(Status.NOT_COMPLETED)) {
            reportCompleted.await();
        }
        return reader;
    }

    public CommandType getCommandType() {
        return commandType;
    }

    private boolean isAtStatus(Status check) {
        reportLock.lock();
        try {
            return status.equals(check);
        } finally {
            reportLock.unlock();
        }
    }

    private Status getStatus() {
        reportLock.lock();
        try {
            return status;
        } finally {
            reportLock.unlock();
        }
    }

    private void setStatus(Status status) {
        reportLock.lock();
        try {
            this.status = status;
        } finally {
            reportCompleted.signalAll();
            reportLock.unlock();
        }
    }


    /**
     * Writer class
     */
    public class Writer {
        private static final Logger logger = LogManager.getLogger(Report.Writer.class);

        private final List<ReportWrittenListener<T>> listeners = new LinkedList<>();
        private final ReentrantLock writerLock = new ReentrantLock();

        public Writer() {

        }

        public void writeNext(T content) throws WriteReportException {
            ifNotWritableThenThrows();
            writerLock.lock();
            try {
                contentIterator.add(content);
                contentIterator.previous();
                contentWritten(content);
            } finally {
                writerLock.unlock();
            }

        }

        public void writeSuccessCompletion() throws WriteReportException {
            ifNotWritableThenThrows();
            writeCompletion(Status.SUCCESS);
        }

        public void writeFailedCompletion() throws WriteReportException {
            writeFailedCompletion(null, null);
        }

        public void writeFailedCompletion(Throwable cause) throws WriteReportException {
            writeFailedCompletion(cause.getMessage(), cause);
        }

        public void writeFailedCompletion(String msg) throws WriteReportException {
            writeFailedCompletion(msg, null);
        }

        public void writeFailedCompletion(String msg, Throwable cause) throws WriteReportException {
            ifNotWritableThenThrows();
            failedMessage = msg;
            failedCause = cause;
            writeCompletion(Status.FAILED);
        }

        private void writeCompletion(Status status) {
            writerLock.lock();
            try {
                setStatus(status);
                completionWritten(reader);
            } finally {
                writerLock.unlock();
            }

        }

        private void ifNotWritableThenThrows() throws WriteReportException {
            if (!isAtStatus(Status.NOT_COMPLETED)) {
                throw new WriteReportException("This report has been completed.");
            }
        }

        public void handleListener(ReportWrittenListener<T> listener) {
            writerLock.lock();
            try {
                contentList.forEach(listener::onContentWritten);
                if (isAtStatus(Status.NOT_COMPLETED)) {
                    listeners.add(listener);
                } else {
                    listener.onCompletionWritten(status, failedMessage, failedCause);
                }
            } finally {
                writerLock.unlock();
            }

        }

        private void contentWritten(T content) throws ReadReportException {
            if (consumer.consumeEarly()) {
                consumer.processPartialContent(reader.readNext());
            }
            listeners.forEach(listener -> listener.onContentWritten(content));
        }

        private void completionWritten(Reader reader) {
            consumer.processCompleteReport(reader);
            listeners.forEach(listener -> listener.onCompletionWritten(status, failedMessage, failedCause));
            listeners.clear();
        }
    }

    /**
     * Reader class
     */
    public class Reader {
        public Reader() {
        }

        public T readNext() throws ReadReportException {
            try {
                return contentIterator.next();
            } catch (NoSuchElementException e) {
                throw new ReadReportException("The next content does not available.");
            }
        }

        public boolean hasNext() {
            return contentIterator.hasNext();
        }

        public Status readStatus() {
            return getStatus();
        }

        public String readFailedMessage() throws ReadReportException {
            if (!isAtStatus(Status.FAILED)) {
                throw new ReadReportException("This report is not failed at time of calling");
            }
            return failedMessage;
        }

        public Throwable readFailedCause() throws ReadReportException {
            if (!isAtStatus(Status.FAILED)) {
                throw new ReadReportException("This report is not failed at time of calling");
            }
            return failedCause;
        }
    }
}


































