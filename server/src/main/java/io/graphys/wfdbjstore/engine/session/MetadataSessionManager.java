package io.graphys.wfdbjstore.engine.session;

import io.graphys.wfdbjstore.engine.session.auth.AuthToken;
import io.graphys.wfdbjstore.engine.session.auth.AuthenticationException;
import io.graphys.wfdbjstore.recordstore.RecordRepository;
import io.graphys.wfdbjstore.recordstore.WfdbStore;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.stream.Collectors;

public final class MetadataSessionManager extends SessionManager<MetadataSession> {

    private static final Logger logger = LogManager.getLogger(MetadataSessionManager.class);
    private final HashMap<String, MetadataSession> sessions = new HashMap<>();

    public MetadataSessionManager(AuthenticationManager authManager, List<WfdbStore> wfdbStores, RecordRepository recordRepo) {
        super(authManager, wfdbStores, recordRepo);
    }

    @Override
    public MetadataSession register(AuthToken token, MetadataSession session) throws AuthenticationException, UnsupportedDatabaseException {
        authenticatedOrElseThrows(token);
        var dbInfo = getDatabase(session.getDbName(), session.getDbVersion());
        if (dbInfo == null) {
            throw new UnsupportedDatabaseException(
                    "The requested database {"
                            + session.getDbName() + "-" + session.getDbVersion()
                            + "} is currently not supported");
        }

        var now = LocalDateTime.now();

        session =  MetadataSession
                .builder()
                .id(generateId())
                .createdAt(now)
                .expiredAt(now.plusHours(DEFAULT_SESSION_HOURS))
                .dbName(dbInfo.name())
                .dbVersion(dbInfo.version())
                .build();

        sessions.put(session.getId(), session);
        return session;
    }

    @Override
    public MetadataSession getSession(String id) {
        return sessions.get(id);
    }

    public boolean existsSession(String sessionId) {
        return sessions.containsKey(sessionId);
    }
}





























