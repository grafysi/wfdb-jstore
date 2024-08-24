package io.graphys.wfdbjstore.dailygreeting;

import io.graphys.wfdbjstore.dailygreeting.domain.Customer;
import io.netty.channel.nio.NioEventLoopGroup;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executors;
import java.util.concurrent.StructuredTaskScope;
import java.util.concurrent.TimeUnit;

public class GreetingClientTest {
    private static final Logger logger = LogManager.getLogger(GreetingClientTest.class);

    @Test
    void testGreetingClient_concurrent() {
        try (var scope = new StructuredTaskScope.ShutdownOnFailure();
             var workerGroup = new NioEventLoopGroup();
        ) {
            for (int i = 0; i < 15_000; i++) {
                var customer = new Customer(i, "Nobi-Nobitbi", "Doraemon Movie", LocalDate.now());
                scope.fork(() -> {
                    var client = new GreetingClient(customer, workerGroup);
                    client.start();
                    return 1;
                });
            }
            scope.join();
            scope.throwIfFailed();
        } catch (Exception e) {
            logger.warn(e);
            throw new RuntimeException(e);
        }
    }

    @Test
    void testGreetingClient() {
        var customer = new Customer(1, "Nobi-Nobibi", "Doraemon Movie", LocalDate.now());
        var client = new GreetingClient(customer, new NioEventLoopGroup());
        client.start();
    }
}
