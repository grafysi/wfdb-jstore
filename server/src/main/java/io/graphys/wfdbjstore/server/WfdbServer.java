/*
package io.graphys.wfdbjstore.server;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import io.graphys.wfdbjstore.recordstore.WfdbManager;
import io.graphys.wfdbjstore.server.codec.NewlineAppendingJsonEncoder;
import io.graphys.wfdbjstore.server.codec.CommandDecoder;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.concurrent.Executors;

public class WfdbServer {
    private static final Logger logger = LogManager.getLogger(WfdbServer.class);
    private final int port;

    public WfdbServer(int port) {
        this.port = port;
    }

    public void start() throws Exception {
        try (var bossGroup = new NioEventLoopGroup();
             var workerGroup = new NioEventLoopGroup();
             var executorService = Executors.newCachedThreadPool()
        ) {
            var objectMapper = new ObjectMapper();
            objectMapper.registerModule(new JavaTimeModule());

            var wfdbManager = WfdbManager.get();
            var commandExecutor = new CommandExecutor(
                    wfdbManager.getWfdbStore("mimic4wdb", "0.1.0"),
                    wfdbManager.getRecordRepository());

            executorService.submit(commandExecutor);

            var bootstrap = new ServerBootstrap();
            bootstrap.group(acceptorGroup, workerGroup)
                    .channel(NioServerSocketChannel.class)
                    .childHandler(new ChannelInitializer<SocketChannel>() {
                        @Override
                        public void initChannel(SocketChannel channel) {
                            channel.pipeline()
                                    .addLast(
                                            new CommandDecoder(),
                                            new NewlineAppendingJsonEncoder(objectMapper),
                                            new ConnectionInitializer(),
                                            new QueryCommandHandler(commandExecutor),
                                            new QueryResultHandler()
                                    );
                        }})
                    .option(ChannelOption.SO_BACKLOG, Integer.MAX_VALUE)
                    .childOption(ChannelOption.SO_KEEPALIVE, true);

            var f = bootstrap.bind(port).sync();
            f.channel().closeFuture().sync();
        } finally {

        }
    }

    public static void main(String[] args) throws Exception {
        int port = 8080;
        logger.info("Run server at localhost:" + port);
        new WfdbServer(8080).start();
    }
}

*/
