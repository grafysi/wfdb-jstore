package io.graphys.wfdbjstore.server.codec;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.graphys.wfdbjstore.protocol.exchange.ExchangeConvention;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToByteEncoder;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

@ChannelHandler.Sharable
public class NewlineAppendingJsonEncoder extends MessageToByteEncoder<Object> {

    private static final Logger logger = LogManager.getLogger(NewlineAppendingJsonEncoder.class);

    private final ObjectMapper jsonMapper;

    public NewlineAppendingJsonEncoder(ObjectMapper objectMapper) {
        this.jsonMapper = objectMapper;
    }


    @Override
    public void encode(ChannelHandlerContext ctx, Object msg, ByteBuf out) throws Exception {
        String frame;
        if (msg instanceof String strMsg) {
            frame = strMsg;
        } else {
            frame = jsonMapper.writeValueAsString(msg);
        }
        frame = frame + "\n";
        out.writeCharSequence(frame, ExchangeConvention.CHARSET);

        logger.debug("Frame sent: {}", frame.substring(0, frame.length() - 1));
    }
}
