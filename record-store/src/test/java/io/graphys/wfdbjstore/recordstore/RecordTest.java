package io.graphys.wfdbjstore.recordstore;

import org.junit.jupiter.api.Test;
import org.springframework.util.StopWatch;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Arrays;
import java.util.Scanner;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

public class RecordTest extends BaseTest {
    private final WfdbStore wfdbStore = wfdbManager.getWfdbStore("mimic4wdb", "0.1.0");

    private final RecordRepository recordRepo = wfdbManager.getRecordRepository();

    private final String CHECK_FILE_NAME = "data/input/81739927.csv";

    @Test
    void testReadSignalInput() {
        var mismatchesLog = "data/logs/81739927_mismatches";
        var stopWatch = new StopWatch();

        stopWatch.start();
        var pathInfo = wfdbStore.findPathInfoOf("81739927");
        assertNotNull(pathInfo);
        var record = recordRepo.findBy(pathInfo);
        stopWatch.stop();
        logger.info("Create record in {} ms.", stopWatch.getTotalTime(TimeUnit.MILLISECONDS));

        /*stopWatch.start();
        var signalInput = record.newSignalInput();
        stopWatch.stop();
        logger.info("Create input signal in {} ms.", stopWatch.getTotalTime(TimeUnit.MILLISECONDS));*/

        try (var printWriter = new PrintWriter(CONSOLE_PATH);
             var signalInput = record.newSignalInput();
             var scanner = new Scanner(new File(CHECK_FILE_NAME));
             var mismatchOut = new PrintWriter(mismatchesLog);
        ) {
            scanner.useDelimiter("\n");
            int[] samples;
            int steps = 20_000;
            int counter = 0;

            stopWatch.start();
            var mismatches = 0;
            for (int i = 0; i < signalInput.getTotalSamples() ; i++) {
                samples = signalInput.readSamples();
                var str = Arrays.stream(samples)
                        .mapToObj(String::valueOf)
                        .collect(Collectors.joining(","));
                printWriter.println(str);
                var check = scanner.nextLine();
                if (!str.equals(check)) {
                    mismatches++;

                    if (mismatches < 1000) {
                        mismatchOut.println("Error at index: " + i);
                        mismatchOut.println("Expected:");
                        mismatchOut.println(check);
                        mismatchOut.println("Actual:");
                        mismatchOut.println(str);
                        mismatchOut.println("---------------------------");
                        /*logger.info("Error at index: {}", i);
                        logger.info("Expected: {}", check);
                        logger.info("Actual: {}", str);*/
                    }
                    logger.info("Error at index: {}", i);
                    logger.info("Expected: {}", check);
                    logger.info("Actual: {}", str);
                    assertEquals(check, str);
                }
                if (++counter % steps == 0) {
                    stopWatch.stop();
                    logger.info("{} samples of block {} done in {} ms.", steps, counter / steps, stopWatch.getTotalTime(TimeUnit.MILLISECONDS));
                    stopWatch.start();
                }
            }

            logger.info("Number of mismatches: {}", mismatches);
            assertEquals(0, mismatches);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        //threadSleep(70_000);
    }

    @Test
    void testReadSignalInput_useSeek() {
        var stopWatch = new StopWatch();

        stopWatch.start();
        var pathInfo = wfdbStore.findPathInfoOf("81739927");
        assertNotNull(pathInfo);
        var record = recordRepo.findBy(pathInfo);
        stopWatch.stop();
        logger.info("Create record in {} ms.", stopWatch.getTotalTime(TimeUnit.MILLISECONDS));

        try (var signalInput = record.newSignalInput();
             var scanner = new Scanner(new File(CHECK_FILE_NAME));
        ) {
            scanner.useDelimiter("\n");
            int passedSamples = 1000;
            for (int i = 0; i < passedSamples; i++) {
                var samples = signalInput.readSamples();
                var checks = Arrays
                        .stream(scanner.nextLine().split(","))
                        .mapToInt(Integer::parseInt).toArray();
                assertEquals(0, Arrays.compare(samples, checks));
            }

            // seek
            var seekNumber = 2312312;
            signalInput.seek(seekNumber);
            for (int i = 0; i < seekNumber - passedSamples; i++) {
                scanner.nextLine();
            }

            var readNext = 2_000_000;
            for (int i = 0; i < readNext; i++) {
                var samples = signalInput.readSamples();
                var str = Arrays.stream(samples).mapToObj(String::valueOf).collect(Collectors.joining(","));
                var check = scanner.nextLine();
                if (!str.equals(check)) {
                    logger.info("Error at index: {}", i);
                    logger.info("Expected: {}", check);
                    logger.info("Actual: {}", str);
                    assertEquals(check, str);
                }
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Test
    void testPrintRecordInfo() {
        var outName = "data/logs/81739927_info";
        var pathInfo = wfdbStore.findPathInfoOf("81739927");
        var record = (MultiSegmentRecord) recordRepo.findBy(pathInfo);

        /*try (var out = new PrintWriter(outName)) {
            for (var seg: record.segments) {
                out.println("----------------" + seg.getName() + "-------------");
                var str = Arrays
                        .stream(seg.signalInfo)
                        .map(SignalInfo::toString)
                        .collect(Collectors.joining("\n----\n"));
                out.println(str);
            }
        } catch (IOException e) {
            throw  new RuntimeException(e);
        }*/
    }

    @Test
    void testSkipAndRead() throws IOException{
        var outName = "data/logs/81739927_info";
        var pathInfo = wfdbStore.findPathInfoOf("81739927");
        var record = (MultiSegmentRecord) recordRepo.findBy(pathInfo);

        try (var signalInput = (MultiSegmentRecord.MultiSegmentSignalInput) record.newSignalInput();
             var out = new PrintWriter(CONSOLE_PATH);
        ) {
            signalInput.seek(2858240);
            /*for (int i = 0; i < record.segments.length; i++) {
                if (signalInput.signalMaps[i] != null) {
                    logger.info("SignalMap-{}: {}", i, signalInput.signalMaps[i]);
                }
            }*/

            for (int i = 0; i < 100; i++) {
                var str = Arrays.stream(signalInput.readSamples())
                        .mapToObj(String::valueOf)
                        .collect(Collectors.joining(","));
                out.println(str);
            }
        }
    }

}




























