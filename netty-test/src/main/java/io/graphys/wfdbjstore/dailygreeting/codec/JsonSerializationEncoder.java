package io.graphys.wfdbjstore.dailygreeting.codec;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToByteEncoder;
import io.netty.handler.codec.MessageToMessageEncoder;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.List;

public class JsonSerializationEncoder<I> extends MessageToByteEncoder<I> {
    private static final Logger logger = LogManager.getLogger(JsonSerializationEncoder.class);
    private final ObjectMapper objectMapper;
    private static final Charset CHARSET = StandardCharsets.UTF_8;
    private final Class<I> outboundMessageType;

    public JsonSerializationEncoder(Class<I> outboundMessageType, ObjectMapper objectMapper) {
        super(outboundMessageType);
        this.outboundMessageType = outboundMessageType;
        this.objectMapper = objectMapper;
    }

    public JsonSerializationEncoder(Class<I> outboundMessageType) {
        super(outboundMessageType);
        this.objectMapper = new ObjectMapper();
        this.outboundMessageType = outboundMessageType;
    }

    @Override
    public void encode(ChannelHandlerContext ctx, I msg, ByteBuf out) {
        try {
            //out.clear();
            //logger.info("Prepare to serialize {}:{}", outboundMessageType, msg);
            var jsonStr =  objectMapper.writeValueAsString(msg);
            out.writeCharSequence(jsonStr, CHARSET);
            //logger.info("Encoded to String:{}", jsonStr);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }

    }
}































