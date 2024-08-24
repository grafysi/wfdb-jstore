package io.graphys.wfdbstore.client;

import io.graphys.wfdbjstore.driver.Wave4jClient;
import io.graphys.wfdbjstore.driver.domain.Command;
import io.graphys.wfdbjstore.driver.domain.Report;
import io.graphys.wfdbjstore.protocol.description.MetadataConnectionDescription;
import io.graphys.wfdbjstore.protocol.description.RecordReadDescription;
import io.graphys.wfdbjstore.protocol.exchange.CommandType;
import io.graphys.wfdbjstore.protocol.exchange.MediaType;
import io.netty.channel.nio.NioEventLoopGroup;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.time.Instant;
import java.util.concurrent.StructuredTaskScope;
import java.util.concurrent.atomic.AtomicInteger;

public class Wave4jClientTest {
    private static final Logger logger = LogManager.getLogger(Wave4jClientTest.class);

    @Test
    public void testBasicCommand() {
        try (
             var scope = new StructuredTaskScope.ShutdownOnFailure();
             var workerGroup = new NioEventLoopGroup()

        ) {
            var ordinal = new AtomicInteger(0);
            for (int i = 0; i < 1; i++) {
                Thread.sleep(3);
                scope.fork(() -> {
                    doSomeCommands(ordinal.getAndIncrement(), workerGroup);
                    return null;
                });
            }

            scope.join();
            scope.throwIfFailed();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        finally {

        }
    }

    private void printReport(Report report) {
        logger.info(report.getCommandType());
        report.getContentList().forEach(logger::info);
    }

    private void doSomeCommands(int ordinal, NioEventLoopGroup workerGroup) throws InterruptedException {
        var client = new Wave4jClient();
        client.connect(workerGroup);

        var start = Instant.now();

        var report = client.execute(
                Command.builder()
                        .commandType(CommandType.INIT_METADATA_CONNECTION)
                        .description(
                                MetadataConnectionDescription.builder()
                                        .scheme("basic")
                                        .token("snowj;abcd1234")
                                        .dbName("mimic4wdb")
                                        .dbVersion("0.1.0")
                                        .reportMediaType(MediaType.APPLICATION_JSON)
                                        .isReactive(true)
                                        .build())
                        .build());
        printReport(report);

        report = client.execute(
                Command.builder()
                        .commandType(CommandType.READ_METADATA_RECORD)
                        .description(
                                RecordReadDescription.builder()
                                        //.name("83268087")
                                        .build())
                        .build());
        printReport(report);

        logger.info("Connection 1 done in {} ms", Duration.between(start, Instant.now()).toMillis());

        client.close().sync();
    }
}
