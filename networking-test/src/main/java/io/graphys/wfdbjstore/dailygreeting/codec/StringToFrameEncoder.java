package io.graphys.wfdbjstore.dailygreeting.codec;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.LengthFieldPrepender;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.List;

public class StringToFrameEncoder extends LengthFieldPrepender {
    private static final Logger logger = LogManager.getLogger(StringToFrameEncoder.class);
    public static final Charset CHARSET = StandardCharsets.UTF_8;
    public static final int LENGTH_FIELD_LENGTH = 2;

    public StringToFrameEncoder() {
        super(LENGTH_FIELD_LENGTH);
    }



    @Override
    public void encode(ChannelHandlerContext ctx, ByteBuf msg, List<Object> out) throws Exception {
        //logger.info("Start encode to bytes from: {}", msg);
        //var lengthFieldBytes = toByteArray(msg.length(), LENGTH_FIELD_LENGTH);
        //out.writeBytes(lengthFieldBytes);
        super.encode(ctx, msg, out);

        //logger.info("Encoded bytebuffer: {}", out.toString(CHARSET));
        //logger.info("Encode to bytes successful...");
    }
}


























