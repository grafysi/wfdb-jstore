package com.grafysi.wfdb.driver;

import com.grafysi.wfdb.driver.domain.Command;
import com.grafysi.wfdb.driver.domain.Report;
import com.grafysi.wfdb.driver.exception.WfdbException;
import io.graphys.wfdbjstore.protocol.content.Content;
import io.graphys.wfdbjstore.protocol.description.Description;
import io.graphys.wfdbjstore.protocol.exchange.CommandType;
import io.graphys.wfdbjstore.protocol.exchange.MediaType;
import io.graphys.wfdbjstore.protocol.exchange.StatusCode;
import lombok.Getter;
import lombok.Setter;

import java.io.Closeable;
import java.time.LocalDateTime;
import java.util.List;

public abstract class Connection implements Closeable {

    @Getter @Setter
    private String sessionId;

    @Getter @Setter
    private LocalDateTime createdAt;

    @Getter @Setter
    private LocalDateTime expiredAt;

    @Getter @Setter
    private String database;

    @Getter @Setter
    private String dbVersion;

    @Getter @Setter
    private MediaType mediaType;

    private Boolean isReactive;

    private final Wave4jClient client;

    @Getter
    private boolean isClosed;

    Connection(Wave4jClient client, Description initDesc) throws WfdbException {
        this.client = client;
        initConnection(initDesc);
    }

    private void initConnection(Description description) throws WfdbException {

        var report = client.execute(createInitCommandFrom(description));
        if (!StatusCode.SUCCESS.equals(report.getStatusCode())) {
            throw new WfdbException("Initialize connection failed with code: " + report.getStatusCode());
        }
        processInitReportContent(report.getContentList().getFirst());
    }

    protected abstract Command createInitCommandFrom(Description initDesc);

    protected abstract void processInitReportContent(Content content);

    public abstract List<CommandType> allowedCommandTypes();

    public final Report executeCommand(Command command) throws WfdbException {

        if (isClosed()) {
            throw new WfdbException("Connection has been closed");
        }

        if (allowedCommandTypes().contains(command.getCommandType())) {
            return client.execute(command);
        }
        throw new WfdbException("Command type not allowed for this connection: " + command.getCommandType());
    }

    public final Report executeCommand(CommandType commandType, Description description) throws WfdbException {

        if (!commandType.getDescriptionClass().equals(description.getClass())) {
            throw new WfdbException(
                    "Command of type " + commandType + " do not support description of class: " + description.getClass());
        }

        var command = Command.builder()
                .commandType(commandType)
                .description(description)
                .build();

        return executeCommand(command);
    }

    @Override
    public void close() {
        try {
            client.close().sync();
            isClosed = true;
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }
}
























