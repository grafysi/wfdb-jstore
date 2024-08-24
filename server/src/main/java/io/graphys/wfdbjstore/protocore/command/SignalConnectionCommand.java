package io.graphys.wfdbjstore.protocore.command;

import io.graphys.wfdbjstore.engine.SessionInitialization;
import io.graphys.wfdbjstore.engine.session.SignalSession;
import io.graphys.wfdbjstore.protocol.content.SignalConnectionContent;
import io.graphys.wfdbjstore.protocol.description.SignalConnectionDescription;
import io.graphys.wfdbjstore.protocol.exchange.CommandType;
import io.graphys.wfdbjstore.protocore.report.ReportConsumer;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public final class SignalConnectionCommand
        extends Command<SignalConnectionDescription, SignalConnectionContent>
        implements SessionInitialization<SignalSession> {

    private final Logger logger = LogManager.getLogger(SignalConnectionCommand.class);

    public SignalConnectionCommand(CommandType commandType, SignalConnectionDescription description, ReportConsumer reportConsumer) {
        super(commandType, description, reportConsumer);
    }

    @Override
    public Class<SignalSession> getSessionClass() {
        return SignalSession.class;
    }

    @Override
    protected void doAndWriteReport() throws Exception {
        // get session registry
        var registry = getSessionRegistry();

        // register session
        var session = registry.register(
                getAuthToken(description.scheme(), description.token()),
                SignalSession.builder()
                        .dbName(description.dbName())
                        .dbVersion(description.dbVersion())
                        .recordName(description.recordName())
                        .build());


        var mediaType = description.reportMediaType();
        mediaType =
                mediaType != null && commandType.getConnectionType().reportSupports(mediaType)
                        ? mediaType
                        : commandType.getConnectionType().getDescriptionMediaTypes()[0];

        // create content
        var content = SignalConnectionContent.builder()
                .sessionId(session.getId())
                .createdAt(session.getCreatedAt())
                .expiredAt(session.getExpiredAt())
                .dbName(session.getDbName())
                .dbVersion(session.getDbVersion())
                .isReactive(description.isReactive())
                .reportMediaType(mediaType)
                .recordName(session.getRecordName())
                .totalSamples(session.getSignalInput().getTotalSamples())
                .build();

        // write content
        writeContent(content);
    }


}






























