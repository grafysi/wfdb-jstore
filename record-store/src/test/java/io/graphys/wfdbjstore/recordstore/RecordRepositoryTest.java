package io.graphys.wfdbjstore.recordstore;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.time.Instant;
import java.time.chrono.IsoChronology;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.StructuredTaskScope;

import static org.junit.jupiter.api.Assertions.*;

public class RecordRepositoryTest extends BaseTest {
    private RecordRepository recordRepo;
    private WfdbStore wfdbStore;
    private CacheContext cacheContext;

    @BeforeEach
    void initObjects() {
        recordRepo = wfdbManager.getRecordRepository();
        wfdbStore = wfdbManager.getWfdbStore("mimic4wdb", "0.1.0");
        cacheContext = CacheContext.get();
    }

    @Test
    void testFindMultiSegmentRecord_concurrent_findBy() {
        var record = "81739927";
        var pathInfo = wfdbStore.findPathInfoOf(record);

        var subTasks = new LinkedList<StructuredTaskScope.Subtask<Record>>();
        try (var scope = new StructuredTaskScope.ShutdownOnFailure()) {
            for (int i = 0; i < 1_000; i++) {
                var subTask = scope.fork(() -> {
                    return recordRepo.findBy(pathInfo);
                });
                subTasks.add(subTask);
            }
            scope.join();
            scope.throwIfFailed();
        } catch (InterruptedException | ExecutionException e) {
            logger.info("stacktrace for debug", e);
        }

        subTasks
                .stream()
                .map(StructuredTaskScope.Subtask::get)
                .forEach(rec -> {
                    assertEquals(7, rec.getSignalInfo().length);
                    assertInstanceOf(MultiSegmentRecord.class, rec);
                });
    }

    @Test
    void testFindARecord() {
        var recordName = "81002096";
        var pathInfo = wfdbStore.findPathInfoOf(recordName);
        var record = recordRepo.findBy(pathInfo);
        logger.info(record);
    }

    @Test
    void testFindAllRecord() {
        for (int i = 0; i < 2; i++) {
            var start = Instant.now();
            var pathInfo = wfdbStore.findAllPathInfo();
            var record = Arrays.stream(pathInfo)
                    .map(p -> recordRepo.findBy(p))
                    .toList();
            logger.info("Round {} done in {} ms", i, Duration.between(start, Instant.now()).toMillis());
        }
    }
}






























