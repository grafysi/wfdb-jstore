package io.graphys.wfdbjstore.dailygreeting;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import io.graphys.wfdbjstore.dailygreeting.codec.FrameToStringDecoder;
import io.graphys.wfdbjstore.dailygreeting.codec.JsonDeserializationDecoder;
import io.graphys.wfdbjstore.dailygreeting.codec.JsonSerializationEncoder;
import io.graphys.wfdbjstore.dailygreeting.codec.StringToFrameEncoder;
import io.graphys.wfdbjstore.dailygreeting.domain.Customer;
import io.graphys.wfdbjstore.dailygreeting.domain.Greeting;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.LengthFieldPrepender;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.time.LocalDate;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executors;
import java.util.concurrent.StructuredTaskScope;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;

public class GreetingClient {
    private static final Logger logger = LogManager.getLogger(GreetingClient.class);
    private static final String HOST = "localhost";
    private static final int PORT = 9867;
    private final Customer customer;
    private final ReentrantLock lock = new ReentrantLock();
    private final Condition channelCompleted = lock.newCondition();
    private final AtomicBoolean completed = new AtomicBoolean(false);
    private final NioEventLoopGroup eventLoopGroup;


    public GreetingClient(Customer customer, NioEventLoopGroup eventLoopGroup) {
        this.customer = customer;
        this.eventLoopGroup = eventLoopGroup;
    }

    public void start() {
        try {
            var objectMapper = new ObjectMapper();
            objectMapper.registerModule(new JavaTimeModule());
            var b = new Bootstrap();
            b
                    .group(eventLoopGroup)
                    .channel(NioSocketChannel.class)
                    .option(ChannelOption.SO_KEEPALIVE, true)
                    .handler(new ChannelInitializer<SocketChannel>() {
                        @Override
                        public void initChannel(SocketChannel ch) throws Exception {
                            ch.pipeline().addLast(
                                    new FrameToStringDecoder(),
                                    new StringToFrameEncoder(),
                                    new JsonDeserializationDecoder<>(Greeting.class, objectMapper),
                                    new JsonSerializationEncoder<>(Customer.class, objectMapper),
                                    new ClientRegisterServiceHandler(customer)
                            );
                        }
                    });
            var channelFuture = b.connect(HOST, PORT).addListener(f -> {
                if (f.isSuccess()) {
                    //logger.info("{}-{} connected...", customer.getName(), customer.getId());
                } else {
                    logger.error("{}-{} cannot connect...", customer.getName(), customer.getId());
                }
            });

            channelFuture.channel().closeFuture().addListener(f -> {
                if (f.isSuccess()) {
                    //logger.info("{}-{} close successful...", customer.getName(), customer.getId());
                } else {
                    logger.error("{}-{} close failed...", customer.getName(), customer.getId());
                }

                lock.lock();
                try {
                    completed.set(true);
                    channelCompleted.signalAll();
                } finally {
                    lock.unlock();
                }
            });

            if (!completed.get()) {
                lock.lock();
                try {
                    channelCompleted.await();
                } finally {
                    lock.unlock();
                }
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        } finally {
        }
    }

    public static void main(String[] args) {
        /*try (var es = Executors.newCachedThreadPool();
             //var scope = new StructuredTaskScope.ShutdownOnFailure();
             var eventLoopGroup = new NioEventLoopGroup()
        ) {
            for (int i = 0; i < 1; i++) {
                var customer = new Customer(i, "Nobi-Nobitbi", "Doraemon Movie", LocalDate.now());
                *//*scope.fork(() -> {
                    var client = new GreetingClient(customer, eventLoopGroup);
                    client.start();
                    return 1;
                });*//*

                es.submit(() -> new GreetingClient(customer, eventLoopGroup).start());
            }

            *//*scope.join();
            scope.throwIfFailed();*//*
            es.shutdown();
            if (es.awaitTermination(120, TimeUnit.SECONDS)) {
                logger.info("All threads terminated before closing.");
            } else {
                logger.warn("Some threads still not terminated...");
            }
        } catch (Exception e) {
            logger.error(e);
            throw new RuntimeException(e);
        }*/

        /*try (var scope = new StructuredTaskScope.ShutdownOnFailure()
        ) {
            for (int i = 0; i < 1000; i++) {
                var customer = new Customer(i, "Nobi-Nobitbi", "Doraemon Movie", LocalDate.now());
                scope.fork(() -> {
                    var client = new GreetingClient(customer, new NioEventLoopGroup());
                    client.start();
                    return 1;
                });
            }
            scope.join();
            scope.throwIfFailed();

        } catch (InterruptedException | ExecutionException e) {
            logger.error(e);
            throw new RuntimeException(e);
        }*/

        try (var eventLoopGroup = new NioEventLoopGroup()) {
            var customer = new Customer(1, "Nobi-Nobibi", "Doraemon Movie", LocalDate.now());
            var client = new GreetingClient(customer, eventLoopGroup);
            client.start();
            logger.info("Check point #4");
        }


        /*try (var out = new PrintWriter("data/output/nobibi-1")) {
            out.println("this is message from nobibi");
        } catch (FileNotFoundException e) {
            throw new RuntimeException(e);
        }*/
    }

    /*public static void main(String[] args) {
        try (var es = Executors.newVirtualThreadPerTaskExecutor();
             var scope = new StructuredTaskScope.ShutdownOnFailure();
             var eventLoopGroup = new NioEventLoopGroup()
        ) {
            for (int i = 0; i < 1; i++) {
                var customer = new Customer(i, "Nobi-Nobitbi", "Doraemon Movie", LocalDate.now());
                scope.fork(() -> {
                    var client = new GreetingClient(customer, eventLoopGroup);
                    client.start();
                    return 1;
                });
            }

            scope.join();
            scope.throwIfFailed();
            *//*es.shutdown();
            if (es.awaitTermination(120, TimeUnit.SECONDS)) {
                logger.info("All threads terminated before closing.");
            } else {
                logger.warn("Some threads still not terminated...");
            }*//*
        } catch (InterruptedException | ExecutionException e) {
            logger.error(e);
            throw new RuntimeException(e);
        }
    }*/
}




































