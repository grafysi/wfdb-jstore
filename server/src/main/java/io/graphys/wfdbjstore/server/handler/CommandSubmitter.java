package io.graphys.wfdbjstore.server.handler;

import io.graphys.wfdbjstore.engine.ExecutionDispatcher;
import io.graphys.wfdbjstore.engine.WfdbExecution;
import io.graphys.wfdbjstore.protocore.command.Command;
import io.graphys.wfdbjstore.server.ConnectionAttr;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

@ChannelHandler.Sharable
public class CommandSubmitter extends ChannelInboundHandlerAdapter {
    private static final Logger logger = LogManager.getLogger(CommandSubmitter.class);

    private final ExecutionDispatcher dispatcher;

    public CommandSubmitter(ExecutionDispatcher dispatcher) {
        this.dispatcher = dispatcher;
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        var command = (Command<?,?>) msg;
        ctx.write(command.getCommandType().getCode());
        var sessionId = (String) ConnectionAttr.SESSION_ID.ofChannel(ctx.channel()).get();
        dispatcher.dispatch((WfdbExecution) msg, sessionId);
    }
}
