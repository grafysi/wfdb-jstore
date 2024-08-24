package io.graphys.wfdbjstore.server;

import io.graphys.wfdbjstore.protocol.content.Content;
import io.graphys.wfdbjstore.protocore.report.Report;
import io.graphys.wfdbjstore.protocore.report.ReportConsumer;
import io.netty.channel.ChannelHandlerContext;

public class BatchReportConsumer implements ReportConsumer {
    private final ChannelHandlerContext context;

    public BatchReportConsumer(ChannelHandlerContext ctx) {
        this.context = ctx;
    }

    @Override
    public boolean consumeEarly() {
        return false;
    }

    @Override
    public void processPartialContent(Content content) {
        throw new IllegalStateException("Batch consumer refused to process partial content.");
    }

    @Override
    public void processCompleteReport(Report<?>.Reader reader) {
        context.fireUserEventTriggered(reader);
    }
}
