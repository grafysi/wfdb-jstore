package io.graphys.wfdbstore.client;

import io.graphys.wfdbjstore.driver.Wave4jClient;
import io.graphys.wfdbjstore.driver.domain.Command;
import io.graphys.wfdbjstore.driver.domain.Report;
import io.graphys.wfdbjstore.protocol.content.ReadSignalFlowContent;
import io.graphys.wfdbjstore.protocol.description.ReadSignalFlowDescription;
import io.graphys.wfdbjstore.protocol.description.SignalConnectionDescription;
import io.graphys.wfdbjstore.protocol.exchange.CommandType;
import io.graphys.wfdbjstore.protocol.exchange.MediaType;
import io.graphys.wfdbjstore.protocol.exchange.StatusCode;
import io.netty.channel.nio.NioEventLoopGroup;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;

public class ReadSignalFlowTest {

    private static final Logger logger = LogManager.getLogger(ReadSignalFlowTest.class);

    private static final String SERVER_HOST = "localhost";

    private static final int SERVER_PORT = 18080;

    private static final String AUTH_SCHEME = "basic";

    private static final String USER = "a_user";

    private static final String PASSWORD = "abcd1234";

    private Wave4jClient client;

    private NioEventLoopGroup usedWorkers;

    @BeforeEach
    void init() throws Exception {
        usedWorkers = new NioEventLoopGroup();
        client = connectClient(usedWorkers);
    }

    @AfterEach
    void cleanup() throws Exception {
        closeClient(client);
        usedWorkers.close();
    }

    private Wave4jClient connectClient(NioEventLoopGroup workers) {
        try {
            var client = new Wave4jClient();
            client.connect(workers, SERVER_HOST, SERVER_PORT);
            return client;
        } catch (InterruptedException e) {
            throw  new RuntimeException(e);
        }
    }

    private void closeClient(Wave4jClient client) {
        try {
            client.close().sync();
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    @Test
    void readSignalOneByOne() {

        var initCommand = buildInitCommand(USER, PASSWORD, "mimic4wdb", "0.1.0", "81739927");
        var initReport = executeCommand(initCommand);

        assertEquals(StatusCode.SUCCESS, initReport.getStatusCode());

        var signalCommand = buildReadSignalCommand(0L, 10L);
        var signalReport = executeCommand(signalCommand);

        assertEquals(StatusCode.SUCCESS, signalReport.getStatusCode());
        assertEquals(10, signalReport.getContentList().size());

        var expectedSamples = new int[] {-32768, -32768, -32768, -32768, -32768, -32768, -2048};

        var frame = getFrame(0, signalReport);

        assertEquals(0, frame.sampleNumber());
        assertArrayEquals(expectedSamples, frame.samples());
    }

    @Test
    void readSignalWithInitialSkip() {

        final var FRAME_SKIP = 352L;

        var initCommand = buildInitCommand(USER, PASSWORD, "mimic4wdb", "0.1.0", "81739927");
        var initReport = executeCommand(initCommand);

        assertEquals(StatusCode.SUCCESS, initReport.getStatusCode());

        var signalCommand = buildReadSignalCommand(FRAME_SKIP, 10L);
        var signalReport = executeCommand(signalCommand);

        assertEquals(StatusCode.SUCCESS, signalReport.getStatusCode());
        assertEquals(10, signalReport.getContentList().size());

        var expectedSamples = new int[] {-32768, 4, -32768, 4, 4, 0, -1025};

        var frame = getFrame(0, signalReport);

        assertEquals(FRAME_SKIP, frame.sampleNumber());
        assertArrayEquals(expectedSamples, frame.samples());
    }

    @Test
    void readSignalWithRepeatedSkips() {

        var initCommand = buildInitCommand(USER, PASSWORD, "mimic4wdb", "0.1.0", "81739927");
        var initReport = executeCommand(initCommand);

        assertEquals(StatusCode.SUCCESS, initReport.getStatusCode());

        final var FRAME_SKIP = 50_000L;
        final var FRAME_LIMIT = 3L;

        for (int i = 0; i < 20; i++) {
            var signalCommand = buildReadSignalCommand(FRAME_SKIP, FRAME_LIMIT);
            var signalReport = executeCommand(signalCommand);

            assertEquals(StatusCode.SUCCESS, signalReport.getStatusCode());
            assertEquals(FRAME_LIMIT, signalReport.getContentList().size());

            var expectedFirstSampleNumber = i * FRAME_LIMIT + (i + 1) * FRAME_SKIP;

            var frame = getFrame(0, signalReport);
            assertEquals(expectedFirstSampleNumber, frame.sampleNumber());
        }
    }



    private ReadSignalFlowContent getFrame(int index, Report report) {
        return (ReadSignalFlowContent) report.getContentList().get(index);
    }



    private Command buildInitCommand(String username, String password, String dbName, String dbVersion, String recordName) {

        var initDesc = SignalConnectionDescription.builder()
                .scheme(AUTH_SCHEME)
                .token(username + ";" + password)
                .dbName(dbName)
                .dbVersion(dbVersion)
                .reportMediaType(MediaType.APPLICATION_JSON)
                .isReactive(true)
                .recordName(recordName)
                .build();

        return Command.builder()
                .commandType(CommandType.INIT_SIGNAL_CONNECTION)
                .description(initDesc)
                .build();
    }

    private Command buildReadSignalCommand(Long frameSkip, Long frameLimit) {

        var signalDesc = ReadSignalFlowDescription.builder()
                .frameSkip(frameSkip)
                .frameLimit(frameLimit)
                .build();

        return Command.builder()
                .commandType(CommandType.READ_SIGNAL_FLOW)
                .description(signalDesc)
                .build();
    }

    private Report executeCommand(Command command) {
        return executeCommand(command, client);
    }

    private Report executeCommand(Command command, Wave4jClient client) {
        logger.info("Execute command: {}", command);
        return client.execute(command);
    }

    private void printReport(Report report) {
        logger.info("Command type: {}", report.getCommandType());
        report.getContentList().forEach(logger::info);
    }
}
























