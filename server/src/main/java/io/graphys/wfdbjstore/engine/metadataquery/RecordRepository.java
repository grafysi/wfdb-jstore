package io.graphys.wfdbjstore.engine.metadataquery;

import io.graphys.wfdbjstore.engine.util.CopyUtils;
import io.graphys.wfdbjstore.recordstore.WfdbStore;

import java.util.Arrays;
import java.util.function.Consumer;
import java.util.function.Predicate;

public class RecordRepository implements MetadataRepository<Record> {
    private final WfdbStore wfdbStore;
    private final io.graphys.wfdbjstore.recordstore.RecordRepository internalRepo;

    public RecordRepository(WfdbStore wfdbStore, io.graphys.wfdbjstore.recordstore.RecordRepository internalRepo) {
        this.wfdbStore = wfdbStore;
        this.internalRepo = internalRepo;
    }

    @Override
    public void findByName(String name, int limit, Consumer<Record> consumer) {
        var pathInfo = wfdbStore.findPathInfoOf(name);
        if (limit > 0 && pathInfo != null) {
            var record = internalRepo.findBy(pathInfo);
            consumer.accept(CopyUtils.copyFrom(record));
        }
    }

    @Override
    public void findByFilter(Predicate<Record> filter, int limit, Consumer<Record> consumer) {
        var pathInfo = wfdbStore.findAllPathInfo();
        Arrays.stream(pathInfo)
                .map(internalRepo::findBy)
                .map(CopyUtils::copyFrom)
                .filter(filter)
                .limit(limit)
                .forEach(consumer);
    }

    @Override
    public void findAll(int limit, Consumer<Record> consumer) {
        var pathInfo = wfdbStore.findAllPathInfo();
        Arrays.stream(pathInfo)
                .limit(limit)
                .map(internalRepo::findBy)
                .map(CopyUtils::copyFrom)
                .forEach(consumer);
    }

    @Override
    public Class<Record> getResultClass() {
        return Record.class;
    }
}





































