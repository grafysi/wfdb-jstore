package io.graphys.wfdbjstore.dailygreeting;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.junit.jupiter.api.Test;

import java.nio.charset.StandardCharsets;

public class ByteBufTest {
    private static final Logger logger = LogManager.getLogger(ByteBufTest.class);

    @Test
    void testRetainSlice() {
        var buffer = Unpooled.buffer();

        buffer.writeCharSequence("Hello, world!", StandardCharsets.UTF_8);
        var slice = buffer.retainedSlice(0, 5);

        buffer.release();
        slice.release();

        logger.info("Original bytes: {}", buffer.toString(StandardCharsets.UTF_8));
        logger.info("Slice bytes: {}", slice.toString(StandardCharsets.UTF_8));

    }
}
