package io.graphys.wfdbjstore.dailygreeting.codec;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.LengthFieldBasedFrameDecoder;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.nio.charset.StandardCharsets;

public class FrameToStringDecoder extends LengthFieldBasedFrameDecoder {
    private static final Logger logger = LogManager.getLogger(FrameToStringDecoder.class);
    public static final int MAX_FRAME_LENGTH = 2000;
    public static final int LENGTH_FIELD_OFFSET = 0;
    public static final int LENGTH_FIELD_LENGTH = 2;
    public static final int LENGTH_ADJUSTMENT = 0;
    public static final int INITIAL_BYTES_TO_STRIP = 2;
    public static final boolean FAIL_FAST = true;

    public FrameToStringDecoder() {
        super(MAX_FRAME_LENGTH,
                LENGTH_FIELD_OFFSET,
                LENGTH_FIELD_LENGTH,
                LENGTH_ADJUSTMENT,
                INITIAL_BYTES_TO_STRIP,
                FAIL_FAST);
    }

    @Override
    public Object decode(ChannelHandlerContext ctx, ByteBuf in) throws Exception {
        //var dupBuf = in.duplicate();
        //dupBuf.resetReaderIndex();
        //logger.info("Input sent, bytes: {}", dupBuf.readableBytes());
        var frame = (ByteBuf) super.decode(ctx, in);
        var result = frame.readCharSequence(frame.readableBytes(), StandardCharsets.UTF_8);
        frame.release();
        return result;
    }
}



























