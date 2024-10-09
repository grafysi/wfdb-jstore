package com.grafysi.wfdb.driver;


import com.grafysi.wfdb.driver.domain.Command;
import com.grafysi.wfdb.driver.domain.Report;
import io.graphys.wfdbjstore.protocol.description.MetadataConnectionDescription;
import io.graphys.wfdbjstore.protocol.description.RecordReadDescription;
import io.graphys.wfdbjstore.protocol.exchange.CommandType;
import io.graphys.wfdbjstore.protocol.exchange.MediaType;
import io.graphys.wfdbjstore.protocol.exchange.StatusCode;
import io.netty.channel.nio.NioEventLoopGroup;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.concurrent.StructuredTaskScope;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class ReadMetadataRecordTest {

    private static final Logger logger = LogManager.getLogger(ReadMetadataRecordTest.class);

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
    void retrieveRecordByName() {

        var initCommand = buildInitCommand(USER, PASSWORD, "mimic4wdb", "0.1.0");
        var initReport = executeCommand(initCommand);

        assertEquals(StatusCode.SUCCESS, initReport.getStatusCode());

        var recordCommand = buildReadRecordCommand("81739927", null, null);
        var recordReport = executeCommand(recordCommand);

        assertEquals(StatusCode.SUCCESS, recordReport.getStatusCode());
        assertEquals(1, recordReport.getContentList().size());
    }

    @Test
    void retrieveRecordByTextInfo() {

        var initCommand = buildInitCommand(USER, PASSWORD, "mimic4wdb", "0.1.0");
        var initReport = executeCommand(initCommand);

        assertEquals(StatusCode.SUCCESS, initReport.getStatusCode());

        var infoPatterns = new String[] {".*subject_id 10039708.*"};

        var recordCommand = buildReadRecordCommand(null, infoPatterns, null);
        var recordReport = executeCommand(recordCommand);

        assertEquals(StatusCode.SUCCESS, recordReport.getStatusCode());
        assertEquals(2, recordReport.getContentList().size());
    }

    @Test
    void retrieveAllWithLimit() {

        var initCommand = buildInitCommand(USER, PASSWORD, "mimic4wdb", "0.1.0");
        var initReport = executeCommand(initCommand);

        assertEquals(StatusCode.SUCCESS, initReport.getStatusCode());

        var recordCommand = buildReadRecordCommand(null, null, 10);
        var recordReport = executeCommand(recordCommand);

        assertEquals(StatusCode.SUCCESS, recordReport.getStatusCode());
        assertEquals(10, recordReport.getContentList().size());
    }

    @Test
    void retrieveRecordsConcurrently() {

        final var CLIENT_CONCURRENCY = 1_000;

        try (var scope = new StructuredTaskScope.ShutdownOnFailure();
             var workers = new NioEventLoopGroup(8);
        ) {
            for (int i = 0; i < CLIENT_CONCURRENCY; i++) {
                Thread.sleep(3);
                scope.fork(() -> {
                    var client = connectClient(workers);

                    var initCommand = buildInitCommand(USER, PASSWORD, "mimic4wdb", "0.1.0");
                    var initReport = executeCommand(initCommand, client);

                    assertEquals(StatusCode.SUCCESS, initReport.getStatusCode());

                    var recordCommand = buildReadRecordCommand(null, null, 3);
                    var recordReport = executeCommand(recordCommand, client);

                    assertEquals(StatusCode.SUCCESS, recordReport.getStatusCode());
                    assertEquals(3, recordReport.getContentList().size());

                    closeClient(client);

                    return null;
                });
            }

            scope.join();
            scope.throwIfFailed();

        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private Command buildInitCommand(String username, String password, String dbName, String dbVersion) {
        var connDesc = MetadataConnectionDescription.builder()
                .scheme(AUTH_SCHEME)
                .token(username + ";" + password)
                .dbName(dbName)
                .dbVersion(dbVersion)
                .reportMediaType(MediaType.APPLICATION_JSON)
                .isReactive(true)
                .build();

        return Command.builder()
                .commandType(CommandType.INIT_METADATA_CONNECTION)
                .description(connDesc)
                .build();
    }

    private Command buildReadRecordCommand(String name, String[] infoPattern, Integer limit) {
        var readDesc = RecordReadDescription.builder()
                .name(name)
                .textInfoPattern(infoPattern)
                .limit(limit)
                .build();

        return Command.builder()
                .commandType(CommandType.READ_METADATA_RECORD)
                .description(readDesc)
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
        logger.info(report.getCommandType());
        report.getContentList().forEach(logger::info);
    }
}
