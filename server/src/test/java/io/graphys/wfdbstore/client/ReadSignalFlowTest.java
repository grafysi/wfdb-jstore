package io.graphys.wfdbstore.client;

import io.graphys.wfdbjstore.driver.Wave4jClient;
import io.graphys.wfdbjstore.driver.domain.Command;
import io.graphys.wfdbjstore.driver.domain.Report;
import io.graphys.wfdbjstore.protocol.description.ReadSignalFlowDescription;
import io.graphys.wfdbjstore.protocol.description.SignalConnectionDescription;
import io.graphys.wfdbjstore.protocol.exchange.CommandType;
import io.graphys.wfdbjstore.protocol.exchange.MediaType;
import io.netty.channel.nio.NioEventLoopGroup;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class ReadSignalFlowTest {

    private static final Logger logger = LogManager.getLogger(ReadSignalFlowTest.class);

    @Test
    void testReadSignalFlow() throws Exception {
        try (var workerGroup = new NioEventLoopGroup()) {

            var client = new Wave4jClient();

            client.connect(workerGroup);

            var description = SignalConnectionDescription.builder()
                    .scheme("basic")
                    .token("snowj;abcd1234")
                    .dbName("mimic4wdb")
                    .dbVersion("0.1.0")
                    .reportMediaType(MediaType.APPLICATION_JSON)
                    .isReactive(true)
                    .recordName("81739927")
                    .build();

            var initCommand = Command.builder()
                    .commandType(CommandType.INIT_SIGNAL_CONNECTION)
                    .description(description)
                    .build();

            var report = client.execute(initCommand);

            assertEquals(1, report.getContentList().size());

            printReport(report);

            var readSignalFlowCommand = Command.builder()
                    .commandType(CommandType.READ_SIGNAL_FLOW)
                    .description(
                            ReadSignalFlowDescription.builder()
                                    .frameSkip(0L)
                                    .frameLimit(10L)
                                    .build())
                    .build();

            for (int i = 0; i < 10; i++) {
                report = client.execute(readSignalFlowCommand);
                assertEquals(10, report.getContentList().size());
                //printReport(report);
                Thread.sleep(100);

            }

            client.close().sync();

        } catch (InterruptedException e) {
            logger.error("Unexpected interruption", e);
        }
    }

    private void printReport(Report report) {
        logger.info("Command type: {}", report.getCommandType());
        report.getContentList().forEach(logger::info);
    }
}
