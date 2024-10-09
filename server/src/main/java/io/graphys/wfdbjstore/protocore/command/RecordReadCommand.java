package io.graphys.wfdbjstore.protocore.command;

import io.graphys.wfdbjstore.engine.MetadataQuery;
import io.graphys.wfdbjstore.engine.metadataquery.Record;
import io.graphys.wfdbjstore.protocol.description.RecordReadDescription;
import io.graphys.wfdbjstore.protocol.exchange.CommandType;
import io.graphys.wfdbjstore.protocol.content.RecordReadContent;
import io.graphys.wfdbjstore.protocore.report.CopyReportWrittenListener;
import io.graphys.wfdbjstore.protocore.report.ReportConsumer;
import io.graphys.wfdbjstore.protocore.report.ReportWrittenListener;
import io.graphys.wfdbjstore.protocore.util.CopyUtils;

import java.util.Arrays;

public final class RecordReadCommand extends Command<RecordReadDescription, RecordReadContent> implements MetadataQuery<Record> {

    public RecordReadCommand(CommandType commandType, RecordReadDescription description, ReportConsumer consumer) {
        super(commandType, description, consumer);
    }

    @Override
    public Class<Record> getResultClass() {
        return Record.class;
    }

    @Override
    public void doAndWriteReport() throws Exception {
        // get repository
        var repo = getMetadataRepository();

        var name = description.name();
        var infoPatterns = description.textInfoPatterns();
        var limit = description.limit() == null ? Integer.MAX_VALUE : description.limit();

        // find by both text info and name
        if (name != null && (infoPatterns != null && infoPatterns.length > 0)) {
            repo.findByFilter(
                    r -> testName(r.getName()) && testTextInfo(r.getTextInfo()),
                    limit,
                    this::writeRecord);
        }

        // find by name only
        if (name != null
                && (infoPatterns == null || infoPatterns.length == 0)) {
            repo.findByName(name, limit, this::writeRecord);
            return;
        }

        // find by text info pattern only
        if (name == null && (infoPatterns != null && infoPatterns.length > 0)) {
            repo.findByFilter(
                    r -> testTextInfo(r.getTextInfo()),
                    limit,
                    this::writeRecord
            );
            return;
        }

        // find all
        repo.findAll(limit, this::writeRecord);
    }

    private void writeRecord(Record record) {
        try {
            writeContent(CopyUtils.copyFrom(record));
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private boolean testName(String name) {
        return name.equals(description.name());
    }

    private boolean testTextInfo(String[] actualInfo) {
        return Arrays
                .stream(description.textInfoPatterns())
                .allMatch(pattern -> anyMatch(pattern, false, actualInfo));
    }

    private boolean anyMatch(String str, boolean matchExact, String... candidates) {
        if (candidates == null) {
            return false;
        }
        return Arrays.stream(candidates)
                .anyMatch(candidate -> matchExact
                        ? candidate.equals(str)
                        : candidate.matches(str));
    }

    @Override
    public boolean shareResult(Object consumer) {
        if (consumer instanceof ReportWrittenListener<?> listener) {
            //noinspection unchecked
            getReport().getWriter().handleListener((ReportWrittenListener<RecordReadContent>) listener);
            return true;
        }
        return false;
    }

    @Override
    public ReportWrittenListener<RecordReadContent> getResultConsumer() {
        return new CopyReportWrittenListener<>(getReport().getWriter());
    }
}

























































