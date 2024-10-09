package com.grafysi.wfdb.driver.codec;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.grafysi.wfdb.driver.domain.Report;
import io.graphys.wfdbjstore.protocol.content.Content;
import io.graphys.wfdbjstore.protocol.exchange.CommandType;
import io.graphys.wfdbjstore.protocol.exchange.ExchangeConvention;
import io.graphys.wfdbjstore.protocol.exchange.StatusCode;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.DecoderException;
import io.netty.handler.codec.LineBasedFrameDecoder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class BasicReportDecoder extends LineBasedFrameDecoder {

    private static final Logger logger = LoggerFactory.getLogger(BasicReportDecoder.class);

    private enum State {
        RD_COMMAND_CODE,
        RD_CONTENT,
        RD_STATUS_CODE,
        RD_FAILED_MESSAGE,
        RD_REPORT_END_TOKEN
    }

    private static final int MAX_FRAME_LENGTH = 1_000_000;
    private final ObjectMapper jsonMapper;
    private Report report;
    private State state = State.RD_COMMAND_CODE;

    public BasicReportDecoder(ObjectMapper jsonMapper) {
        super(MAX_FRAME_LENGTH);
        this.jsonMapper = jsonMapper;
    }

    @Override
    public Object decode(ChannelHandlerContext ctx, ByteBuf buffer) throws Exception {
        var frame = (ByteBuf) super.decode(ctx, buffer);
        if (frame == null) {
            return null;
        }
        var message = frame.readCharSequence(frame.readableBytes(), ExchangeConvention.CHARSET).toString();
        frame.release();
        return switch (state) {
            case RD_COMMAND_CODE -> {
                var cmdType = CommandType.getInstanceOf(message);
                if (cmdType == null) {
                    logger.info("Message:" + message);
                    throw new DecoderException("Read command code failed. Message: " + message);
                }

                report = new Report(cmdType);
                state = State.RD_CONTENT;

                logger.info("RD_COMMAND_CODE | Message: {}", message);
                yield null;
            }
            case RD_CONTENT -> {
                if (message.equals(ExchangeConvention.CONTENT_END_TOKEN)) {
                    state = State.RD_STATUS_CODE;
                } else {
                    var content = jsonMapper.readValue(message, report.getCommandType().getContentClass());
                    report.addContent((Content) content);
                }

                logger.info("RD_CONTENT | Message: {}", message);
                yield null;
            }
            case RD_STATUS_CODE -> {
                var statusCode = StatusCode.getInstanceOf(message);
                if (statusCode == null) {
                    throw new DecoderException("Read status code failed");
                }
                report.setStatusCode(statusCode);
                state = State.RD_FAILED_MESSAGE;

                logger.info("RD_STATUS_CODE | Message: {}", message);
                yield null;
            }
            case RD_FAILED_MESSAGE -> {
                report.setFailedMessage(message);
                state = State.RD_REPORT_END_TOKEN;

                logger.info("RD_FAILED_MESSAGE | Message: {}", message);
                yield null;
            }
            case RD_REPORT_END_TOKEN -> {
                if (!message.equals(ExchangeConvention.REPORT_END_TOKEN)) {
                    throw new DecoderException("Read report end token failed");
                }
                var result = report;
                report = null;
                state = State.RD_COMMAND_CODE;

                logger.info("RD_REPORT_END_TOKEN | Message: {}", message);
                yield result;
            }
        };
    }
}































