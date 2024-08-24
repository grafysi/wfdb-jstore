package io.graphys.wfdbjstore.server;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import io.graphys.wfdbjstore.engine.*;
import io.graphys.wfdbjstore.engine.session.AuthenticationManager;
import io.graphys.wfdbjstore.engine.session.MetadataSession;
import io.graphys.wfdbjstore.protocol.exchange.ConnectionType;
import io.graphys.wfdbjstore.recordstore.WfdbManager;
import io.graphys.wfdbjstore.server.codec.CommandDecoder;
import io.graphys.wfdbjstore.server.codec.NewlineAppendingJsonEncoder;
import io.graphys.wfdbjstore.server.handler.BasicReportProcessor;
import io.graphys.wfdbjstore.server.handler.CommandSubmitter;
import io.graphys.wfdbjstore.server.handler.CommandValidator;
import io.graphys.wfdbjstore.server.handler.InitializationReportProcessor;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.util.AttributeKey;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.Arrays;

public class Wave4jServer {
    public static final String COMMAND_DECODER = "COMMAND_DECODER";
    public static final String COMMAND_VALIDATOR = "COMMAND_VALIDATOR";
    public static final String COMMAND_SUBMITTER = "COMMAND_SUBMITTER";
    public static final String REPORT_PROCESSOR = "REPORT_PROCESSOR";
    public static final String JSON_ENCODER = "JSON_ENCODER";

    private static final Logger logger = LogManager.getLogger(Wave4jServer.class);

    private final String host;
    private final int port;

    public Wave4jServer(String host, int port) {
        this.host = host;
        this.port = port;
    }

    public void start() throws Exception {
        // set jackson mapper
        var objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());

        // get wfdb manager
        var wfdbManager = WfdbManager.get();

        // instantiate executors's dependencies
        var authManager = new AuthenticationManager(wfdbManager.getCreditialPath());

        // execution context
        var executionContext = new ExecutionContext(wfdbManager, authManager);

        try (var bossGroup = new NioEventLoopGroup();
             var workerGroup = new NioEventLoopGroup();
             var metadataQueryExecutor = new MetadataQueryExecutor(executionContext.getSessionManager(MetadataSession.class), executionContext);
             var sessionSafeExecutor = new SessionSafeExecutor(executionContext);
        ) {
            // executors
            var sessionInitializationExecutor = new SessionInitializationExecutor(executionContext);

            // dispatcher
            var executionDispatcher = new ExecutionDispatcherImpl(
                    sessionInitializationExecutor, metadataQueryExecutor, sessionSafeExecutor);

            // sharable channel handlers
            var commandValidator = new CommandValidator();
            var commandSubmitter = new CommandSubmitter(executionDispatcher);
            var initializationReportProcessor = new InitializationReportProcessor();
            var jsonEncoder = new NewlineAppendingJsonEncoder(objectMapper);

            // sharable but added later
            var basicReportProcessor = new BasicReportProcessor();

            // instantiate bootstrap
            var bootstrap = new ServerBootstrap();

            // init attribute keys
            Arrays.stream(ConnectionAttr.values()).map(ConnectionAttr::key).forEach(AttributeKey::newInstance);

            bootstrap.group(bossGroup, workerGroup)
                    .channel(NioServerSocketChannel.class)
                    .childHandler(new ChannelInitializer<SocketChannel>() {
                        @Override
                        public void initChannel(SocketChannel channel) {
                            try {
                                /*// sharable channel handlers
                                var commandValidator = new CommandValidator();
                                var commandSubmitter = new CommandSubmitter(executionDispatcher);
                                var initializationReportProcessor = new InitializationReportProcessor();
                                var jsonEncoder = new NewlineAppendingJsonEncoder(objectMapper);

                                // sharable but added later
                                var basicReportProcessor = new BasicReportProcessor();*/

                                var pipeline = channel.pipeline();

                                // add handlers
                                pipeline.addLast(COMMAND_DECODER, new CommandDecoder(objectMapper));
                                pipeline.addLast(JSON_ENCODER, jsonEncoder);
                                pipeline.addLast(COMMAND_VALIDATOR, commandValidator);
                                pipeline.addLast(COMMAND_SUBMITTER, commandSubmitter);
                                pipeline.addLast(REPORT_PROCESSOR, initializationReportProcessor);

                                // consumer context
                                var consumerContext = pipeline.context(COMMAND_SUBMITTER);

                                // report consumers
                                var batchConsumer = new BatchReportConsumer(consumerContext);
                                //var reactiveConsumer = new ReactiveReportConsumer(consumerContext);


                                // set bootstrap attr
                                ConnectionAttr.REPORT_CONSUMER.ofChannel(channel).set(batchConsumer);
                                ConnectionAttr.SESSION_ID.ofChannel(channel).set(null);
                                ConnectionAttr.REPORT_MEDIA_TYPE.ofChannel(channel).set(null);
                                ConnectionAttr.CONNECTION_TYPE.ofChannel(channel).set(ConnectionType.NOT_INITIALIZED);
                                ConnectionAttr.BASIC_REPORT_PROCESSOR.ofChannel(channel).set(basicReportProcessor);

                                /*channel.attr(AttributeKey.newInstance(ConnectionAttr.REPORT_CONSUMER.key())).set(batchConsumer);
                                channel.attr(AttributeKey.newInstance(ConnectionAttr.SESSION_ID.key())).set(null);
                                channel.attr(AttributeKey.newInstance(ConnectionAttr.REPORT_MEDIA_TYPE.key())).set(null);
                                channel.attr(AttributeKey.newInstance(ConnectionAttr.CONNECTION_TYPE.key())).set(ConnectionType.NOT_INITIALIZED);
                                channel.attr(AttributeKey.newInstance(ConnectionAttr.BASIC_REPORT_PROCESSOR.key())).set(basicReportProcessor);*/
                            } catch (Exception e) {
                                logger.error("error", e);
                                throw e;
                            }
                        }
                    })
                    .option(ChannelOption.SO_BACKLOG, Integer.MAX_VALUE)
                    .childOption(ChannelOption.SO_KEEPALIVE, true);

            var f = bootstrap.bind(host, port).sync();
            f.channel().closeFuture().sync();
        }
    }

    public static void main(String[] args) throws Exception {
        var host = "localhost";
        var port = 18080;
        logger.info("Server started at {}:{}", host, port);
        var server = new Wave4jServer(host, port);
        server.start();
    }
}






























