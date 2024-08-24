package io.graphys.wfdbjstore.dailygreeting.codec;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToMessageDecoder;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.List;

@ChannelHandler.Sharable
public class JsonDeserializationDecoder<T> extends MessageToMessageDecoder<String> {
    private static final Logger logger = LogManager.getLogger(JsonDeserializationDecoder.class);
    private final ObjectMapper objectMapper;
    private final Class<T> targetType;

    public JsonDeserializationDecoder(Class<T> targetType) {
        this.targetType = targetType;
        this.objectMapper = new ObjectMapper();
    }

    public JsonDeserializationDecoder(Class<T> targetType, ObjectMapper objectMapper) {
        this.targetType = targetType;
        this.objectMapper = objectMapper;
    }

    @Override
    public void decode(ChannelHandlerContext ctx, String msg, List<Object> out) {
        try {
            //logger.info("Prepare decode msg:{}", msg);
            var readObj = objectMapper.readValue(msg, targetType);
            //logger.info("Decoded {}:{}", targetType, readObj);
            out.add(readObj);
        } catch (JsonProcessingException e) {
            logger.info(e);
            throw new RuntimeException(e);
        }
    }
}

















