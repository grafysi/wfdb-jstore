package io.graphys.wfdbjstore.recordstore;

import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.time.Duration;
import java.time.Instant;
import java.util.Arrays;
import java.util.concurrent.StructuredTaskScope;
import wfdb.*;

public class SignalInputTest extends BaseTest {

    private final WfdbStore wfdbStore = wfdbManager.getWfdbStore("mimic4wdb", "0.1.0");
    private final RecordRepository recordRepo = wfdbManager.getRecordRepository();


    @Test
    public void testReadAllSamplesOfRecords() {
        try (var scope = new StructuredTaskScope.ShutdownOnFailure()) {
            var pathInfo = wfdbStore.findAllPathInfo();
            var records = Arrays.stream(pathInfo).map(recordRepo::findBy).toList();

            var start = Instant.now();
            for (var record: records) {
                scope.fork(() -> {
                    readAllSamplesOf(record);
                    return 1;
                });
            }
            scope.join();
            scope.throwIfFailed();

            var totalSamples = records.stream().mapToLong(Record::getTotalSamples).sum();

            logger.info("Read total {} samples in {} ms", totalSamples, Duration.between(start, Instant.now()).toMillis());

        } catch (Exception e) {
            logger.error("error", e);
        }
    }

    @Test
    public void testReadAllSamples() {
        var pi = wfdbStore.findPathInfoOf("81739927");
        var record = recordRepo.findBy(pi);
        readAllSamplesOf(record);
    }


    void readAllSamplesOf(Record record) {
        try (var input = record.newSignalInput()) {
            //logger.info("Total samples {} of {}", input.getTotalSamples(), pi.getRecordName());
            //logger.info("Avail samples: {}", input.availSamples());

            var start = Instant.now();
            var totalSample = input.getTotalSamples();
            for (int i = 0; i < totalSample; i++) {
                input.readSamples();
                /*if ((i + 1) % 1_000_000 == 0) {
                    logger.info("Sample count {}", i + 1);
                }*/
            }
            logger.info("Read {} samples of record {} in {} ms", input.getTotalSamples(), record.getName(), Duration.between(start, Instant.now()).toMillis());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Test
    void testReadAllSamples_useNative() {
        var pi = wfdbStore.findPathInfoOf("81002096");
        readAllSamples_usingNative(pi);
    }

    void readAllSamples_usingNative(PathInfo pi) {
        //wfdb.setwfdb(wfdbManager.getRemoteWfdbPath());
        var recPath = pi.getAbsoluteDir() + pi.getRecordName();
        var nSig = wfdb.isigopen(recPath, null, 0);

        var siArray = new WFDB_SiginfoArray(nSig);
        // open signals
        wfdb.isigopen(recPath, siArray.cast(), nSig);

        var vector = new WFDB_SampleArray(nSig);

        var start = Instant.now();
        var sampleCount = 0L;
        while (wfdb.getvec(vector.cast()) > 0) {
            sampleCount++;
            if (sampleCount % 1_000_000 == 0) {
                logger.info("Sample count {}", sampleCount);
            }
        }

        logger.info("Read {} samples of {} in {} ms",
                sampleCount, pi.getRecordName(), Duration.between(start, Instant.now()).toMillis());


    }
}



































