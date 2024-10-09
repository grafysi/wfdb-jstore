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

public class ReadSignalSeekTest {

    private static final Logger logger = LogManager.getLogger(ReadSignalSeekTest.class);

    @Test
    void testSeekSample() {
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

            var initCommand = Command.builder()
                    .commandType(CommandType.INIT_SIGNAL_CONNECTION)
                    .description(description)
                    .build();

            var report = client.execute(initCommand);

            printReport(report);

            // Seek to sample 1000
            //var readSignalSeekCommand =


            var readSignalFlowCommand = Command.builder()
                    .commandType(CommandType.READ_SIGNAL_FLOW)
                    .description(
                            ReadSignalFlowDescription.builder()
                                    .frameSkip(9657L + 500L) // break at 9655-9656
                                    .frameLimit(5L)
                                    .build())
                    .build();

            for (int i = 0; i < 30; i++) {
                logger.info("----------- Signal Flow {} -----------", i);
                client.execute(readSignalFlowCommand);
                Thread.sleep(100);
            }

//            readSignalFlowCommand = Command.builder()
//                    .commandType(CommandType.READ_SIGNAL_FLOW)
//                    .description(
//                            ReadSignalFlowDescription.builder()
//                                    .frameSkip(10000L)
//                                    .frameLimit(3L)
//                                    .build())
//                    .build();
//
//            client.execute(readSignalFlowCommand);

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
