package io.graphys.wfdbjstore.recordstore;

import io.graphys.wfdbjstore.recordstore.header.HeaderReader;
import io.graphys.wfdbjstore.recordstore.header.NativeHeaderReader;
import io.graphys.wfdbjstore.recordstore.header.ReadHeader;
import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.time.Instant;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.StructuredTaskScope;
import java.util.concurrent.StructuredTaskScope.Subtask;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

public class HeaderReaderTest extends BaseTest {
    private final HeaderReader headerReader = new NativeHeaderReader();
    private final WfdbStore wfdbStore = wfdbManager.getWfdbStore("mimic4wdb", "0.1.0");
    private final StorageContext storageContext = StorageContext.get();

    @Test
    void testReadSegmentInfo() {
        var pathInfo = wfdbStore.findPathInfoOf("81739927");
        var segmentInfo = headerReader.readSegmentInfo(pathInfo.getRecordPath());
        Arrays.stream(segmentInfo).forEach(logger::info);
    }

    @Test
    void testReadMultiSegment_concurrent() {
        var pathInfo = wfdbStore.findPathInfoOf("81739927");
        var segmentInfo = headerReader.readSegmentInfo(pathInfo.getRecordPath());

        var subTasks = new LinkedList<Subtask<SegmentedRecord>>();
        try (var scope = new StructuredTaskScope.ShutdownOnFailure()) {
            for (var sei: segmentInfo) {
                var subTask = scope.fork(() -> {
                    storageContext.awaitPrepareHeader(sei.getRecordName(), pathInfo);
                    var signalInfo = headerReader.readSignalInfo(pathInfo.getAbsoluteDir() + sei.getRecordName());
                    return SegmentedRecord
                            .segmentBuilder()
                            .name(sei.getRecordName())
                            .pathInfo(pathInfo)
                            .signalInfo(signalInfo)
                            .build();
                });
                subTasks.add(subTask);
            }

            scope.join();
            scope.throwIfFailed();
        } catch (InterruptedException | ExecutionException e) {
            logger.info("test error", e);
        }

        Record layoutSegment = null;
        List<Record> records = new LinkedList<>();
        for (var subTask: subTasks) {
            var record = subTask.get();
            if (record.getName().equals(segmentInfo[0].getRecordName())) {
                layoutSegment = record;
            } else {
                records.add(record);
            }
        }

        assertNotNull(layoutSegment);
        assertEquals(20, records.size());
        logger.info(layoutSegment);
        records.forEach(logger::info);
    }

    @Test
    void testReadMultiSegment_concurrent_multipleTimes() {
        var pathInfo = wfdbStore.findPathInfoOf("81739927");

        try (var scope = new StructuredTaskScope.ShutdownOnFailure()) {
            for (int ignoreMe = 0; ignoreMe < 1_000; ignoreMe++) {
                scope.fork(() -> {
                    /*var segmentNames = IntStream
                            .range(0, 21)
                            .mapToObj(i -> String.format("%s_%04d", pathInfo.getRecordName(), i))
                            .toList();*/

                    var segmentNames =
                            Arrays
                                    .stream(headerReader.readSegmentInfo(pathInfo.getAbsoluteDir() + pathInfo.getRecordName()))
                                    .map(SegmentInfo::getRecordName)
                                    .toList();

                    var miniSubTasks = new LinkedList<Subtask<SegmentedRecord>>();
                    try (var miniScope = new StructuredTaskScope.ShutdownOnFailure()) {
                        /*var miniSubTask = miniScope.fork(() -> {
                            for (var name: segmentNames) {
                                var signalInfo = headerReader.readSignalInfo(pathInfo.getAbsoluteDir() + name);
                            }
                            return SegmentedRecord
                                    .segmentBuilder()
                                    .pathInfo(pathInfo)
                                    .signalInfo();
                        });*/
                        for (var name: segmentNames) {
                            miniScope.fork(() -> {
                                var signalInfo = headerReader.readSignalInfo(pathInfo.getAbsoluteDir() + name);
                                return signalInfo;
                            });
                        }
                        miniScope.join();
                        miniScope.throwIfFailed();
                    }
                    return null;
                });
                scope.join();
                scope.throwIfFailed();
            }
        } catch (Exception e) {
            logger.info("error", e);
            throw new RuntimeException(e);
        }
    }

    @Test
    void testReadFullHeader() {
        var pathInfo = wfdbStore.findPathInfoOf("81739927");
        var reader = new NativeHeaderReader();
        var header = reader.readFullHeader(pathInfo.getAbsoluteDir() + pathInfo.getRecordName());

        logger.info(header.getInfoList());
        logger.info("baseTimeStr: {}", header.getBaseTimeStr());
        //logger.info("baseTime: {}", ReadHeader.parseDataTime(header.getBaseTimeStr()));
        logger.info("baseTime: {}", ReadHeader.parseDataTime("09:00:17.230 16/08/2015"));
        logger.info(header.createMultiSegmentRecord(pathInfo, new SegmentInfo[]{}, new SegmentedRecord[]{}));
    }

    @Test
    void testReadAllHeader() throws InterruptedException {
        var reader = new NativeHeaderReader();
        for (int round = 0; round < 3; round++) {
            readAllRecords(round, reader);
        }

        Thread.sleep(5_000);
    }

    public void readAllRecords(int round, NativeHeaderReader reader) {
        var start = Instant.now();
        var pathInfo = wfdbStore.findAllPathInfo();

        for (var pi: pathInfo) {
            var segmentInfo = reader.readSegmentInfo(pi.getAbsoluteDir() + pi.getRecordName());
            var recordNames = Arrays.stream(segmentInfo)
                    .map(SegmentInfo::getRecordName)
                    .filter(s -> !s.equals("~"))
                    .toArray(String[]::new);

            List<ReadHeader> headers = new LinkedList<>();
            for (int i = 0; i < recordNames.length; i++) {
                var headerName = recordNames[i];
                var recordName = i == 0 ? pi.getRecordName() : recordNames[i];
                //storageContext.awaitPrepareHeader(headerName, pi);
                var readHeader = headerReader.readFullHeader(pi.getAbsoluteDir() + recordName);
                headers.add(readHeader);
            }

            var msHeader = headers.removeFirst();
            var segments = headers.stream().map(h -> h.createSegmentedRecord(pi, null)).toArray(SegmentedRecord[]::new);

            msHeader.createMultiSegmentRecord(pi, segmentInfo, segments);
        }

        logger.info("Round {} done in {} ms", round, Duration.between(start, Instant.now()).toMillis());
    }

    @Test
    void testReadOneRecord() throws InterruptedException {
        var reader = new NativeHeaderReader();
        for (int round = 0; round < 100; round++) {
            readOneRecord(round, reader);
        }
        Thread.sleep(180_000);
    }

    public void readOneRecord(int round, NativeHeaderReader reader) {
        //var pathInfo = wfdbStore.findPathInfoOf("83268087");

        var oddPathInfo = wfdbStore.findPathInfoOf("83268087");
        var evenPathInfo = wfdbStore.findPathInfoOf("88501826");
        var oddRecord = "83268087_0002";
        var evenRecord = "83268087_0004.hea";
        var start = Instant.now();
        /*for (int ui = 0; ui < 200; ui++) {

        }*/

        for (var pathInfo: Arrays.copyOfRange(wfdbStore.findAllPathInfo(), 0, 60)/*int i = 0; i < 500; i++*/) {
            /*if (i % 2 == 0) {
                pathInfo = evenPathInfo;
            } else {
                pathInfo = oddPathInfo;
            }*/
            /*for (int ig = 0; ig < 50; ig++) {

            }*/

            var segmentInfo = reader.readSegmentInfo(pathInfo.getAbsoluteDir() + pathInfo.getRecordName());
            String recordName;
            /*if (i % 2 == 0) {
                recordName = evenRecord;
            } else {
                recordName = oddRecord;
            }*/

            for (var si: segmentInfo) {
                if (!si.getRecordName().equals("~")) {

                    try {
                        recordName = si.getRecordName();
                        var header = reader.readFullHeader(pathInfo.getAbsoluteDir() + recordName);
                        header.createSegmentedRecord(pathInfo, recordName);
                    } catch (Exception e) {
                        logger.error("error", e);
                    } finally {
                    }
                }
            }
        }
        logger.info("Round {} done in {} ms", round, Duration.between(start, Instant.now()).toMillis());
    }

    @Test
    public void testMemoryConsumptionWithString() throws InterruptedException {
        for (int round = 0; round < 50; round++) {
            makeStrings(round);
        }
        Thread.sleep(60_000);
    }

    public void makeStrings(int round) {
        var start = Instant.now();
        for (int i = 0; i < 20_000_000; i++) {
            var uuid = UUID.randomUUID().toString();
        }
        logger.info("Round {} done in {} ms", round, Duration.between(start, Instant.now()).toMillis());
    }
}



























