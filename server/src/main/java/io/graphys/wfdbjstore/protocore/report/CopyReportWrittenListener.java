package io.graphys.wfdbjstore.protocore.report;

import io.graphys.wfdbjstore.protocol.content.Content;

public class CopyReportWrittenListener<T extends Content> implements ReportWrittenListener<T> {
    private final Report<T>.Writer copyingWriter;
    private boolean hasError = false;

    public CopyReportWrittenListener(Report<T>.Writer copyingWriter) {
        this.copyingWriter = copyingWriter;
    }

    @Override
    public void onContentWritten(T content) {
        if (hasError) {
            return;
        }

        try {
            copyingWriter.writeNext(content);
        } catch (Exception e) {
            hasError = true;
            throw new IllegalStateException("Unexpected state, implementation bug!", e);
        }
    }

    @Override
    public void onCompletionWritten(Report.Status status, String failedMessage, Throwable cause) {
        if (hasError) {
            return;
        }

        try {
            if (status == Report.Status.FAILED) {
                copyingWriter.writeFailedCompletion(failedMessage, cause);
            } else {
                copyingWriter.writeSuccessCompletion();
            }
        } catch (Exception e) {
            hasError = true;
            throw new IllegalStateException("Unexpected state, implementation bug!", e);
        }
    }
}






















