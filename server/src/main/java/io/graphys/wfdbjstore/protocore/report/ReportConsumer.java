package io.graphys.wfdbjstore.protocore.report;

import io.graphys.wfdbjstore.protocol.content.Content;

public interface ReportConsumer {
    public void processPartialContent(Content content);

    public void processCompleteReport(Report<?>.Reader reader);

    public boolean consumeEarly();
}

