package com.grafysi.wfdb.driver.codec;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.grafysi.wfdb.driver.domain.Command;
import io.graphys.wfdbjstore.protocol.exchange.ExchangeConvention;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToByteEncoder;

public class CommandEncoder extends MessageToByteEncoder<Command> {
    private final ObjectMapper jsonMapper;

    public CommandEncoder(ObjectMapper jsonMapper) {
        this.jsonMapper = jsonMapper;
    }

    @Override
    public void encode(ChannelHandlerContext ctx, Command command, ByteBuf out) throws Exception {
        var builder = new StringBuilder();
        builder.append(command.getCommandType().getCode());
        builder.append(jsonMapper.writeValueAsString(command.getDescription()));
        builder.append("\n");
        out.writeCharSequence(builder.toString(), ExchangeConvention.CHARSET);
    }
}
