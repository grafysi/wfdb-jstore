package io.graphys.wfdbstore.client;

import io.graphys.wfdbjstore.driver.Wave4jClient;
import io.graphys.wfdbjstore.driver.domain.Command;
import io.graphys.wfdbjstore.driver.domain.Report;
import io.graphys.wfdbjstore.protocol.description.SignalConnectionDescription;
import io.graphys.wfdbjstore.protocol.exchange.CommandType;
import io.graphys.wfdbjstore.protocol.exchange.MediaType;
import io.netty.channel.nio.NioEventLoopGroup;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class InitSignalConnectionTest {

    private static final Logger logger = LogManager.getLogger(InitSignalConnectionTest.class);

    @Test
    void testInitSignalConnection() {
        try (var workerGroup = new NioEventLoopGroup()) {

            var client = new Wave4jClient();

            client.connect(workerGroup, "localhost", 18080);

            var description = SignalConnectionDescription.builder()
                    .scheme("basic")
                    .token("snowj;abcd1234")
                    .dbName("mimic4wdb")
                    .dbVersion("0.1.0")
                    .reportMediaType(MediaType.APPLICATION_JSON)
                    .isReactive(true)
                    .recordName("81739927")
                    .build();

            var command = Command.builder()
                    .commandType(CommandType.INIT_SIGNAL_CONNECTION)
                    .description(description)
                    .build();

            var report = client.execute(command);

            assertEquals(1, report.getContentList().size());

            printReport(report);

            client.close().sync();

        } catch (InterruptedException e) {
            logger.error("Unexpected interruption", e);
        }
    }


    private void printReport(Report report) {
        logger.info(report.getCommandType());
        report.getContentList().forEach(logger::info);
    }
}



































