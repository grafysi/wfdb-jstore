package io.graphys.wfdbjstore.dailygreeting.codec;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.ByteToMessageDecoder;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.List;

public class JsonStringDecoder extends ByteToMessageDecoder {
    private static final Logger logger = LogManager.getLogger(JsonStringDecoder.class);
    private static final Charset CHARSET = StandardCharsets.UTF_8;
    @Override
    public void decode(ChannelHandlerContext ctx, ByteBuf in, List<Object> out) {
        // the message body is previously detected and pass to this handler
        // no need to do further check
        //logger.info("Prepare decoding bytes here...");
        logger.info("Get frame, size: {}", in.readableBytes());
        var str = in.readCharSequence(in.readableBytes(), CHARSET);
        //logger.info("Check point #1");
        //in.discardReadBytes();
        //var dupBuf = in;//.duplicate();
        //dupBuf.resetReaderIndex();
        //logger.info("Check point #2");
        logger.info("The json string message:{}", str);
        //logger.info("Size of input buffer: {}", dupBuf.toString(CHARSET).length());
        out.add(str);
    }
}
