package io.graphys.wfdbjstore.protocore.command;

import io.graphys.wfdbjstore.engine.SessionInitialization;
import io.graphys.wfdbjstore.engine.session.MetadataSession;
import io.graphys.wfdbjstore.protocol.content.MetadataConnectionContent;
import io.graphys.wfdbjstore.protocol.description.MetadataConnectionDescription;
import io.graphys.wfdbjstore.protocol.exchange.CommandType;
import io.graphys.wfdbjstore.protocore.report.ReportConsumer;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public final class MetadataConnectionCommand
        extends Command<MetadataConnectionDescription, MetadataConnectionContent>
        implements SessionInitialization<MetadataSession> {

    private static final Logger logger = LogManager.getLogger(MetadataConnectionCommand.class);

    public MetadataConnectionCommand(
            CommandType commandType, MetadataConnectionDescription description, ReportConsumer consumer) {
        super(commandType, description, consumer);
    }

    @Override
    public Class<MetadataSession> getSessionClass() {
        return MetadataSession.class;
    }

    @Override
    protected void doAndWriteReport() throws Exception {
        // get session registry
        var registry = getSessionRegistry();
        MetadataSession session = null;

        // register new session
        session = registry.register(
                getAuthToken(description.scheme(), description.token()),
                MetadataSession
                        .builder()
                        .dbName(description.dbName())
                        .dbVersion(description.dbVersion())
                        .build());

        // get media type
        var mediaType = description.reportMediaType();
        mediaType =
                mediaType != null && commandType.getConnectionType().reportSupports(mediaType)
                ? mediaType
                : commandType.getConnectionType().getDescriptionMediaTypes()[0];

        // create content
        var content = MetadataConnectionContent
                .builder()
                .sessionId(session.getId())
                .createdAt(session.getCreatedAt())
                .expiredAt(session.getExpiredAt())
                .dbName(session.getDbName())
                .dbVersion(session.getDbVersion())
                .isReactive(description.isReactive())
                .reportMediaType(mediaType)
                .build();

        // write content
        writeContent(content);
    }
}























