package com.grafysi.wfdb.driver;

import com.grafysi.wfdb.driver.domain.Command;
import com.grafysi.wfdb.driver.exception.WfdbException;
import io.graphys.wfdbjstore.protocol.content.Content;
import io.graphys.wfdbjstore.protocol.content.MetadataConnectionContent;
import io.graphys.wfdbjstore.protocol.content.RecordReadContent;
import io.graphys.wfdbjstore.protocol.description.Description;
import io.graphys.wfdbjstore.protocol.description.MetadataConnectionDescription;
import io.graphys.wfdbjstore.protocol.description.RecordReadDescription;
import io.graphys.wfdbjstore.protocol.exchange.CommandType;
import io.graphys.wfdbjstore.protocol.exchange.StatusCode;

import java.util.List;

public class MetadataConnection extends Connection {

    private static final List<CommandType> ALLOWED_COMMAND_TYPES = List.of(CommandType.READ_METADATA_RECORD);

    public MetadataConnection(Wave4jClient client, MetadataConnectionDescription description) throws WfdbException {
        super(client, description);
    }

    @Override
    public List<CommandType> allowedCommandTypes() {
        return ALLOWED_COMMAND_TYPES;
    }

    @Override
    public Command createInitCommandFrom(Description description) {
        return Command.builder()
                .commandType(CommandType.INIT_METADATA_CONNECTION)
                .description(description)
                .build();
    }

    @Override
    public void processInitReportContent(Content content) {
        var in = (MetadataConnectionContent) content;
        setSessionId(in.sessionId());
        setDatabase(in.dbName());
        setDbVersion(in.dbVersion());
        setCreatedAt(in.createdAt());
        setExpiredAt(in.expiredAt());
        setMediaType(in.reportMediaType());
    }

    public List<RecordReadContent> runReadRecordCommand(String name, List<String> infoPatterns, Integer limit) throws WfdbException {

        var description = RecordReadDescription.builder()
                .name(name)
                .textInfoPatterns(infoPatterns.toArray(new String[0]))
                .limit(limit)
                .build();

        var command = Command.builder()
                .commandType(CommandType.READ_METADATA_RECORD)
                .description(description)
                .build();

        var report = executeCommand(command);

        if (!StatusCode.SUCCESS.equals(report.getStatusCode())) {
            throw new WfdbException("Execute command failed with message: " + report.getFailedMessage());
        }

        try {
            return report.getContentList()
                    .stream()
                    .map(c -> (RecordReadContent) c)
                    .toList();
        } catch (Exception e) {
            throw new WfdbException(e);
        }

    }
}























