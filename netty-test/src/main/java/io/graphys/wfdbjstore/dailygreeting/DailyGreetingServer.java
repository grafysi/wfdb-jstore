package io.graphys.wfdbjstore.dailygreeting;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import io.graphys.wfdbjstore.dailygreeting.codec.FrameToStringDecoder;
import io.graphys.wfdbjstore.dailygreeting.codec.JsonDeserializationDecoder;
import io.graphys.wfdbjstore.dailygreeting.codec.JsonSerializationEncoder;
import io.graphys.wfdbjstore.dailygreeting.codec.StringToFrameEncoder;
import io.graphys.wfdbjstore.dailygreeting.domain.Customer;
import io.graphys.wfdbjstore.dailygreeting.domain.Greeting;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.LengthFieldPrepender;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class DailyGreetingServer {
    private static final Logger logger = LogManager.getLogger(DailyGreetingServer.class);
    private final int port;

    public DailyGreetingServer(int port) {
        this.port = port;
    }

    public static void main(String[] args) throws Exception {
        int port = 9867;
        logger.info("Start server at 127.0.0.1:{}", port);
        new DailyGreetingServer(port).start();
    }

    public void start() throws Exception {
        try (var acceptorGroup = new NioEventLoopGroup();
             var workerGroup = new NioEventLoopGroup();
        ) {
            var objectMapper = new ObjectMapper();
            objectMapper.registerModule(new JavaTimeModule());
            var b = new ServerBootstrap();
            b
                    .group(acceptorGroup, workerGroup)
                    .channel(NioServerSocketChannel.class)
                    .childHandler(new ChannelInitializer<SocketChannel>() {
                        @Override
                        public void initChannel(SocketChannel ch) {
                            ch.pipeline().addLast(
                                    /*new LengthFieldBasedFrameDecoder(
                                            StringToFrameEncoder.LENGTH_FIELD_MAX_VALUE, 0,
                                            StringToFrameEncoder.LENGTH_FIELD_LENGTH, 0,
                                            StringToFrameEncoder.LENGTH_FIELD_LENGTH),*/
                                    new FrameToStringDecoder(),
                                    new StringToFrameEncoder(),
                                    new JsonDeserializationDecoder<>(Customer.class, objectMapper),
                                    new JsonSerializationEncoder<>(Greeting.class, objectMapper),
                                    new RegisterCustomerHandler()
                            );
                        }
                    })
                    .option(ChannelOption.SO_BACKLOG, Integer.MAX_VALUE)
                    .childOption(ChannelOption.SO_KEEPALIVE, true);
            var f = b.bind(port).sync();
            f.channel().closeFuture().sync();
        } finally {

        }
    }
}

























