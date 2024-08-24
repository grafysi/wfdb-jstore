package io.graphys.wfdbjstore.server.handler;

import io.graphys.wfdbjstore.protocol.content.Content;
import io.graphys.wfdbjstore.protocol.exchange.ExchangeConvention;
import io.graphys.wfdbjstore.protocol.exchange.StatusCode;
import io.graphys.wfdbjstore.protocore.report.Report;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

@ChannelHandler.Sharable
public class BasicReportProcessor extends ChannelInboundHandlerAdapter {
    private static final Logger logger = LogManager.getLogger(BasicReportProcessor.class);

    @Override
    public final void userEventTriggered(ChannelHandlerContext ctx, Object msg) {
        if (msg instanceof Content content) {
            processPartialContent(ctx, content);
        } else if (msg instanceof Report<?>.Reader reader) {
            processCompleteReport(ctx, reader);
        }
    }

    protected void processPartialContent(ChannelHandlerContext ctx, Content content) {
        ctx.writeAndFlush(content);
    }

    protected void processCompleteReport(ChannelHandlerContext ctx, Report<?>.Reader reader) {
        while (reader.hasNext()) {
            ctx.write(reader.readNext());
        }
        ctx.write(ExchangeConvention.CONTENT_END_TOKEN);
        if (reader.readStatus() == Report.Status.FAILED) {
            ctx.write(StatusCode.SERVER_ERROR.getCode());
            ctx.write(reader.readFailedMessage() == null ? "" : reader.readFailedMessage());
        } else {
            ctx.write(StatusCode.SUCCESS.getCode());
            ctx.write("");
        }
        ctx.write(ExchangeConvention.REPORT_END_TOKEN);
        ctx.flush();
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        logger.error("error", cause);
    }
}





























