package io.graphys.wfdbjstore.protocore.report;

import io.graphys.wfdbjstore.protocol.content.Content;

public interface ReportWrittenListener<T extends Content> {
    public void onContentWritten(T content);

    public void onCompletionWritten(Report.Status status, String failedMessage, Throwable failedCause);
}
