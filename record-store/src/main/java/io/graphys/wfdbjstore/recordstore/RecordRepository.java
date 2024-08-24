package io.graphys.wfdbjstore.recordstore;

import io.graphys.wfdbjstore.recordstore.exception.RecordConstructFailedException;
import io.graphys.wfdbjstore.recordstore.header.HeaderReader;
import io.graphys.wfdbjstore.recordstore.header.ReadHeader;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.StructuredTaskScope;
import java.util.concurrent.StructuredTaskScope.Subtask;
import java.util.stream.IntStream;


public class RecordRepository {
    private static final Logger logger = LogManager.getLogger(RecordRepository.class);
    private final HeaderReader headerReader;
    private final StorageContext storageContext;
    private final CacheContext cacheContext;

    public RecordRepository(HeaderReader headerReader) {
        this.headerReader = headerReader;
        this.storageContext = StorageContext.get();
        this.cacheContext = CacheContext.get();
    }

    /*public Record findBy(PathInfo pathInfo) {
        try (var scope = new StructuredTaskScope.ShutdownOnFailure()) {
            storageContext.awaitPrepareHeader(pathInfo.getRecordName(), pathInfo);
            if (headerReader.isMultiSegmentRecord(pathInfo.getRecordPath())) {
                var segmentInfo = headerReader.readSegmentInfo(pathInfo.getAbsoluteDir() + pathInfo.getRecordName());
                var subTasks = new ArrayList<StructuredTaskScope.Subtask<SegmentedRecord>>(segmentInfo.length);
                for (var sei: segmentInfo) {
                    var subTask = scope.fork(() -> {
                        storageContext.awaitPrepareHeader(sei.getRecordName(), pathInfo);
                        var signalInfo = headerReader.readSignalInfo(pathInfo.getAbsoluteDir() + sei.getRecordName());
                        return SegmentedRecord
                                .segmentBuilder()
                                .name(sei.getRecordName())
                                .signalInfo(signalInfo)
                                .build();
                    });
                    subTasks.add(subTask);
                }
                scope.join();
                scope.throwIfFailed();
                return MultiSegmentRecord
                        .multiSegmentBuilder()
                        .pathInfo(pathInfo)
                        .segmentInfo(segmentInfo)
                        .signalInfo(subTasks.removeFirst().get().getSignalInfo())
                        .segments(subTasks.stream().map(Subtask::get).toArray(SegmentedRecord[]::new))
                        .build();
            } else {
                return OrdinaryRecord
                        .builder()
                        .pathInfo(pathInfo)
                        .signalInfo(headerReader.readSignalInfo(pathInfo.getRecordPath()))
                        .build();
            }
        } catch (IOException | ExecutionException | InterruptedException e) {
            throw new RecordConstructFailedException("Construct record failed.", e);
        }
    }*/

    public Record findBy(PathInfo pathInfo) {
        var cachedRecord = cacheContext.retrieveRecord(pathInfo.getRecordName(), pathInfo.getDbInfo());
        if (cachedRecord != null) {
            return cachedRecord;
        }
        try {
            storageContext.awaitPrepareHeader(pathInfo.getRecordName(), pathInfo);

            // when call read segment info for un cached record, the wfdblib error may occur: init: can't open header for record xxxxxx_0000
            // as read segment function of native lib auto refer to _0000 header in case of multi-segment record
            var segmentInfo = headerReader.readSegmentInfo(pathInfo.getRecordPath());
            /**
             * There is bug when reading: init: can't open header for record mimic4wdb/0.1.0/waves/p165/p16566444/80057524/~
             * due to segment file name start with ~
             * so the following code filter out segments that have name started with ~
             */
            segmentInfo = Arrays.stream(segmentInfo).filter(s -> !s.getRecordName().equals("~")).toArray(SegmentInfo[]::new);
            /**
             * Debug end
             */
            Record resultRecord;
            if (segmentInfo.length > 0) {
                var subTasks = new ArrayList<Subtask<ReadHeader>>();
                var recordNames = Arrays
                        .stream(segmentInfo)
                        .map(SegmentInfo::getRecordName)
                        .toArray(String[]::new);
                try (var scope = new StructuredTaskScope.ShutdownOnFailure()) {
                    /*for (var recordName: recordNames) {
                        var subTask = scope.fork(() -> {
                            storageContext.awaitPrepareHeader(recordName, pathInfo);
                            *//*try {
                                Thread.sleep(10);
                            } catch (InterruptedException e) {throw new RuntimeException(e);}*//*
                            return headerReader.readFullHeader(pathInfo.getAbsoluteDir() + recordName);
                        });
                        subTasks.add(subTask);
                    }*/

                    //var headers = new ArrayList<ReadHeader>();

                    var position = 0;
                    var nThreads = 10;
                    while (position < recordNames.length) {
                        for (int i = position; i < Math.min(position + nThreads, recordNames.length); i++) {
                            var headerName = recordNames[i];
                            var recordName = i == 0 ? pathInfo.getRecordName() : recordNames[i];

                            var subTask = scope.fork(() -> {
                                storageContext.awaitPrepareHeader(headerName, pathInfo);
                                return headerReader.readFullHeader(pathInfo.getAbsoluteDir() + recordName);
                            });
                            subTasks.add(subTask);
                        }
                        scope.join();
                        scope.throwIfFailed();
                        position += nThreads;
                    }

                    /*for (int i = 0; i < recordNames.length; i++) {
                        var headerName = recordNames[i];
                        var recordName = i == 0 ? pathInfo.getRecordName() : recordNames[i];
                        *//*var subTask = scope.fork(() -> {
                            storageContext.awaitPrepareHeader(headerName, pathInfo);
                            return headerReader.readFullHeader(pathInfo.getAbsoluteDir() + recordName);
                        });
                        subTasks.add(subTask);*//*

                        storageContext.awaitPrepareHeader(headerName, pathInfo);
                        var readHeader = headerReader.readFullHeader(pathInfo.getAbsoluteDir() + recordName);
                        headers.add(readHeader);
                    }*/
                    /*scope.join();
                    scope.throwIfFailed();*/

                    var headers = subTasks.stream().map(Subtask::get).toList();

                    var msHeader = headers.getFirst();
                    var segments = IntStream
                            .range(1, recordNames.length)
                            .mapToObj(i -> {
                                return headers.get(i).createSegmentedRecord(pathInfo, recordNames[i]);
                            })
                            .toArray(SegmentedRecord[]::new);
                    resultRecord = msHeader.createMultiSegmentRecord(pathInfo, segmentInfo, segments);
                    //return resultRecord;
                } catch (Exception e) {
                    logger.error("error", e);
                    throw new RecordConstructFailedException("Construct record failed.", e);
                }
            } else {
                var readMsHeader = headerReader.readFullHeader(pathInfo.getRecordPath());
                resultRecord = readMsHeader.createOrdinaryRecord(pathInfo);
            }
            cacheContext.cacheRecord(resultRecord, resultRecord.getName(), resultRecord.getPathInfo().getDbInfo());
            return resultRecord;
        } catch (IOException e) {
            throw new RecordConstructFailedException("Construct record failed.", e);
        }
    }
}































