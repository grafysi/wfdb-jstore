package io.graphys.wfdbjstore.server.codec;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.graphys.wfdbjstore.protocol.description.MetadataConnectionDescription;
import io.graphys.wfdbjstore.protocol.description.ReadSignalFlowDescription;
import io.graphys.wfdbjstore.protocol.description.RecordReadDescription;
import io.graphys.wfdbjstore.protocol.description.SignalConnectionDescription;
import io.graphys.wfdbjstore.protocol.exchange.CommandType;
import io.graphys.wfdbjstore.protocore.command.*;
import io.graphys.wfdbjstore.protocol.exchange.ExchangeConvention;
import io.graphys.wfdbjstore.protocore.report.ReportConsumer;
import io.graphys.wfdbjstore.server.ConnectionAttr;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.DelimiterBasedFrameDecoder;
import io.netty.handler.codec.Delimiters;

public class CommandDecoder extends DelimiterBasedFrameDecoder {
    private static final Logger logger = LogManager.getLogger(CommandDecoder.class);
    private static final int MAX_FRAME_LENGTH = 20000;
    private final ObjectMapper jsonMapper;

    public CommandDecoder(ObjectMapper objectMapper) {
        super(MAX_FRAME_LENGTH, true, Delimiters.lineDelimiter());
        this.jsonMapper = objectMapper;
    }

    @Override
    public Command<?, ?> decode(ChannelHandlerContext ctx, ByteBuf buffer) throws Exception {
        var frame = (ByteBuf) super.decode(ctx, buffer);
        if (frame == null) {
            return null;
        }
        var message = frame.readCharSequence(
                frame.readableBytes(), ExchangeConvention.CHARSET).toString();
        logger.info("Received bytes: {}", message);

        var commandCode = message.substring(0, CommandType.COMMAND_CODE_SIZE);
        var payload = message.substring(CommandType.COMMAND_CODE_SIZE);

        var commandType = CommandType.getInstanceOf(commandCode);
        var consumer = (ReportConsumer) ConnectionAttr.REPORT_CONSUMER.ofChannel(ctx.channel()).get();

        var command = switch (commandType) {
            case INIT_METADATA_CONNECTION -> {
                var description = jsonMapper.readValue(payload, MetadataConnectionDescription.class);
                //noinspection unchecked
                yield new MetadataConnectionCommand(commandType, description, consumer);
            }
            case READ_METADATA_RECORD -> {
                var description = jsonMapper.readValue(payload, RecordReadDescription.class);
                //noinspection unchecked
                yield new RecordReadCommand(commandType, description, consumer);
            }
            case INIT_SIGNAL_CONNECTION -> {
                var description = jsonMapper.readValue(payload, SignalConnectionDescription.class);
                //noinspection unchecked
                yield new SignalConnectionCommand(commandType, description, consumer);
            }
            case READ_SIGNAL_FLOW -> {
                var description = jsonMapper.readValue(payload, ReadSignalFlowDescription.class);
                //noinspection unchecked
                yield new ReadSignalFlowCommand(commandType, description, consumer);
            }
            case null, default -> throw new IllegalStateException("Implementation bug");
        };
        frame.release();
        return command;
    }
}























