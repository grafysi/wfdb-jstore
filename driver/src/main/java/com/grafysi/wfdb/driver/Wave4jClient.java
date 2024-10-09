package com.grafysi.wfdb.driver;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.grafysi.wfdb.driver.codec.BasicReportDecoder;
import com.grafysi.wfdb.driver.codec.CommandEncoder;
import com.grafysi.wfdb.driver.domain.Command;
import com.grafysi.wfdb.driver.domain.Report;
import com.grafysi.wfdb.driver.exception.WfdbException;
import com.grafysi.wfdb.driver.handler.BasicClientHandler;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class Wave4jClient {

    private static final Logger logger = LogManager.getLogger(Wave4jClient.class);

    private BasicClientHandler clientHandler = new BasicClientHandler();

    private ObjectMapper objectMapper;

    public Wave4jClient() {
        this.objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());
    }

    Wave4jClient(NioEventLoopGroup workers, String host, int port, ObjectMapper objectMapper) throws WfdbException {

        this.objectMapper = objectMapper;
        try {
            connect(workers, host, port);
        } catch (Exception e) {
            throw new WfdbException(e);
        }
    }

    public void connect(NioEventLoopGroup workerGroup, final String HOST, final int PORT) throws InterruptedException {
        //var objectMapper = new ObjectMapper();
        //objectMapper.registerModule(new JavaTimeModule());

        var bootstrap = new Bootstrap();

        bootstrap.group(workerGroup)
                .channel(NioSocketChannel.class)
                .option(ChannelOption.SO_KEEPALIVE, true)
                .handler(new ChannelInitializer<SocketChannel>() {
                    @Override
                    public void initChannel(SocketChannel channel) {
                        var pipeline = channel.pipeline();
                        pipeline.addLast(new CommandEncoder(objectMapper));
                        pipeline.addLast(new BasicReportDecoder(objectMapper));
                        pipeline.addLast(clientHandler);
                        clientHandler.setContext(pipeline.context(clientHandler));
                    }});



        var connectFuture = bootstrap.connect(HOST, PORT).sync();
        connectFuture.addListener(f -> {
            if (f.isSuccess()) {
                logger.info("Connected to {}:{}", HOST, PORT);
            } else {
                logger.error("Connect failed.");
            }});

        var closeFuture = connectFuture.channel().closeFuture().addListener(f -> {
            if (f.isSuccess()) {
                logger.info("Channel closed with success...");
            } else {
                logger.info("Channel closed with failure...");
            }
        });
    }

    public Report execute(Command command) {
        try {
            return clientHandler.awaitSendCommand(command);
        } catch (InterruptedException e) {
            return null;
        }
    }

    public ChannelFuture close() throws InterruptedException {
        return clientHandler.closeChannel();
    }
}





















