package com.grafysi.wfdb.driver;

import com.grafysi.wfdb.driver.domain.Command;
import com.grafysi.wfdb.driver.exception.WfdbException;
import io.graphys.wfdbjstore.protocol.content.Content;
import io.graphys.wfdbjstore.protocol.content.ReadSignalFlowContent;
import io.graphys.wfdbjstore.protocol.content.SignalConnectionContent;
import io.graphys.wfdbjstore.protocol.description.Description;
import io.graphys.wfdbjstore.protocol.description.ReadSignalFlowDescription;
import io.graphys.wfdbjstore.protocol.description.SignalConnectionDescription;
import io.graphys.wfdbjstore.protocol.exchange.CommandType;
import io.graphys.wfdbjstore.protocol.exchange.StatusCode;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

public class SignalConnection extends Connection {

    private static final List<CommandType> ALLOWED_COMMAND_TYPES = List.of(CommandType.READ_SIGNAL_FLOW);

    @Getter @Setter
    private String recordName;

    public SignalConnection(Wave4jClient client, SignalConnectionDescription description) throws WfdbException {
        super(client, description);
    }

    @Override
    public List<CommandType> allowedCommandTypes() {
        return ALLOWED_COMMAND_TYPES;
    }

    @Override
    protected Command createInitCommandFrom(Description description) {
        return Command.builder()
                .commandType(CommandType.INIT_SIGNAL_CONNECTION)
                .description(description)
                .build();
    }

    @Override
    public void processInitReportContent(Content content) {
        var in = (SignalConnectionContent) content;
        setSessionId(in.sessionId());
        setDatabase(in.dbName());
        setDbVersion(in.dbVersion());
        setCreatedAt(in.createdAt());
        setExpiredAt(in.expiredAt());
        setMediaType(in.reportMediaType());
        setRecordName(in.recordName());
    }

    public List<ReadSignalFlowContent> runReadSignalFlowCommand(Long frameSkip, Long frameLimit) throws WfdbException {

        var description = ReadSignalFlowDescription.builder()
                .frameSkip(frameSkip)
                .frameLimit(frameLimit)
                .build();

        var command = Command.builder()
                .commandType(CommandType.READ_SIGNAL_FLOW)
                .description(description)
                .build();

        var report = executeCommand(command);

        if (!StatusCode.SUCCESS.equals(report.getStatusCode())) {
            throw new WfdbException("Execute command failed with message: " + report.getFailedMessage());
        }

        try {
            return report.getContentList()
                    .stream()
                    .map(c -> (ReadSignalFlowContent) c)
                    .toList();
        } catch (Exception e) {
            throw new WfdbException(e);
        }
    }
}


























