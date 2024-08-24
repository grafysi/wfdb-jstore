package io.graphys.wfdbjstore.server.handler;

import io.graphys.wfdbjstore.protocol.content.Content;
import io.graphys.wfdbjstore.protocol.content.MetadataConnectionContent;
import io.graphys.wfdbjstore.protocol.content.SignalConnectionContent;
import io.graphys.wfdbjstore.protocol.exchange.ConnectionType;
import io.graphys.wfdbjstore.protocol.exchange.MediaType;
import io.graphys.wfdbjstore.protocore.report.Report;
import io.graphys.wfdbjstore.server.ConnectionAttr;
import io.graphys.wfdbjstore.server.Wave4jServer;
import io.graphys.wfdbjstore.server.ReactiveReportConsumer;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

@ChannelHandler.Sharable
public class InitializationReportProcessor extends BasicReportProcessor {

    private static final Logger logger = LogManager.getLogger(InitializationReportProcessor.class);

    @Override
    public void processPartialContent(ChannelHandlerContext ctx, Content content) {
        throw new IllegalStateException("Initialization processor refused process partial content");
    }

    @Override
    public void processCompleteReport(ChannelHandlerContext ctx, Report<?>.Reader reader) {
        var content = reader.readNext();

        switch (content) {
            case MetadataConnectionContent mcc -> {
                setupConnection(ctx, ConnectionType.METADATA, mcc.isReactive(), mcc.reportMediaType(), mcc.sessionId());
                ctx.write(mcc);
            }
            case SignalConnectionContent scc -> {
                setupConnection(ctx, ConnectionType.SIGNAL, scc.isReactive(), scc.reportMediaType(), scc.sessionId());
                ctx.write(scc);
            }
            case null, default -> {
                throw new RuntimeException("Unexpected runtime behaviour");
            }
        }

        // send as normal
        super.processCompleteReport(ctx, reader);

        // replace this handler by
        var basicProcessor = (ChannelHandler) ConnectionAttr.BASIC_REPORT_PROCESSOR.ofChannel(ctx.channel()).get();

        if (basicProcessor == null) {
            throw new IllegalStateException("Unexpected behavior occurred.");
        }
        ctx.pipeline().replace(Wave4jServer.REPORT_PROCESSOR, Wave4jServer.REPORT_PROCESSOR, basicProcessor);
    }

    private void setupConnection(ChannelHandlerContext ctx, ConnectionType connType, boolean isReactive, MediaType reportMediaType, String sessionId) {
        // change consumer if reactive transfer specified
        if (isReactive) {
            var reactiveConsumer = new ReactiveReportConsumer(ctx.pipeline().context(Wave4jServer.COMMAND_SUBMITTER));
            ConnectionAttr.REPORT_CONSUMER.ofChannel(ctx.channel()).set(reactiveConsumer);
        }

        // set connection type
        ConnectionAttr.CONNECTION_TYPE.ofChannel(ctx.channel()).set(connType);

        // set report content media type
        ConnectionAttr.REPORT_MEDIA_TYPE.ofChannel(ctx.channel()).set(reportMediaType);

        // set session id
        ConnectionAttr.SESSION_ID.ofChannel(ctx.channel()).set(sessionId);
    }

}






















