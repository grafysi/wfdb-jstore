package io.graphys.wfdbjstore.server.handler;

import io.graphys.wfdbjstore.engine.MetadataQuery;
import io.graphys.wfdbjstore.engine.SessionInitialization;
import io.graphys.wfdbjstore.engine.SignalQuery;
import io.graphys.wfdbjstore.protocol.exchange.ConnectionType;
import io.graphys.wfdbjstore.server.ConnectionAttr;
import io.graphys.wfdbjstore.server.IllegalCommandException;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

@ChannelHandler.Sharable
public class CommandValidator extends ChannelInboundHandlerAdapter {
    private static final Logger logger = LogManager.getLogger(CommandValidator.class);


    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        var connType = (ConnectionType) ConnectionAttr.CONNECTION_TYPE.ofChannel(ctx.channel()).get();
        switch (connType) {
            case NOT_INITIALIZED -> {
                if (! (msg instanceof SessionInitialization<?>)) {
                    throw new IllegalCommandException("Only allow initialization command for no session connection");
                }
            }
            case METADATA -> {
                if (! (msg instanceof MetadataQuery<?>)) {
                    throw new IllegalCommandException("Only allow metadata query command in this session");
                }
            }
            case SIGNAL -> {
                if (! (msg instanceof SignalQuery)) {
                    throw new IllegalCommandException("Only allow signal query command in current session");
                }
            }
        }
        ctx.fireChannelRead(msg);
    }
}

































