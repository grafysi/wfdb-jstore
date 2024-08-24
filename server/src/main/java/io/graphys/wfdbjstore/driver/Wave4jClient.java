package io.graphys.wfdbjstore.driver;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import io.graphys.wfdbjstore.driver.codec.BasicReportDecoder;
import io.graphys.wfdbjstore.driver.codec.CommandEncoder;
import io.graphys.wfdbjstore.driver.domain.Command;
import io.graphys.wfdbjstore.driver.domain.Report;
import io.graphys.wfdbjstore.driver.handler.BasicClientHandler;
import io.netty.channel.ChannelFuture;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class Wave4jClient {
    private static final Logger logger = LogManager.getLogger(Wave4jClient.class);
    private static final String HOST = "localhost";
    private static final int PORT = 18080;
    private BasicClientHandler clientHandler = new BasicClientHandler();

    public Wave4jClient() {

    }

    public void connect(NioEventLoopGroup workerGroup) throws InterruptedException {
        var objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());

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





















