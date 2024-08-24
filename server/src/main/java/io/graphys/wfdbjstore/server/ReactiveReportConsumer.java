package io.graphys.wfdbjstore.server;

import io.graphys.wfdbjstore.protocol.content.Content;
import io.graphys.wfdbjstore.protocore.report.Report;
import io.graphys.wfdbjstore.protocore.report.ReportConsumer;
import io.netty.channel.ChannelHandlerContext;

public class ReactiveReportConsumer implements ReportConsumer {

    private final ChannelHandlerContext context;

    public ReactiveReportConsumer(ChannelHandlerContext ctx) {
        this.context = ctx;
    }

    @Override
    public boolean consumeEarly() {
        return true;
    }

    @Override
    public void processPartialContent(Content content) {
        context.fireUserEventTriggered(content);
    }

    @Override
    public void processCompleteReport(Report<?>.Reader reader) {
        context.fireUserEventTriggered(reader);
    }
}
