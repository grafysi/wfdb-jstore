package io.graphys.wfdbjstore.engine.session;

import io.graphys.wfdbjstore.engine.session.auth.AuthToken;
import io.graphys.wfdbjstore.engine.session.auth.AuthenticationException;
import io.graphys.wfdbjstore.recordstore.RecordRepository;
import io.graphys.wfdbjstore.recordstore.WfdbStore;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public final class SignalSessionManager extends SessionManager<SignalSession> {

    private static final Logger logger = LogManager.getLogger(SignalSessionManager.class);

    private final Map<String, SignalSession> sessions = Collections.synchronizedMap(new HashMap<>());

    public SignalSessionManager(AuthenticationManager authManager, List<WfdbStore> wfdbStores, RecordRepository recordRepo) {
        super(authManager, wfdbStores, recordRepo);
    }

    @Override
    public SignalSession getSession(String id) {
        return sessions.get(id);
    }

    @Override
    public SignalSession register(AuthToken authToken, SignalSession session)
            throws AuthenticationException, RecordNotFoundException, UnsupportedDatabaseException  {

        authenticatedOrElseThrows(authToken);

        var dbInfo = getDatabase(session.getDbName(), session.getDbVersion());

        if (dbInfo == null) {
            throw new UnsupportedDatabaseException(
                    "The requested database {"
                            + session.getDbName() + "-" + session.getDbVersion()
                            + "} is currently not supported");
        }

        var record = getRecord(session.getRecordName(), dbInfo);

        if (record == null) {
            throw new RecordNotFoundException(
                    "Not found record {name=" + session.getRecordName() +
                            ", database=" + session.getDbName() +
                            ", version=" + session.getDbVersion() + "}");
        }

        var signalInput = record.newSignalInput();

        var now = LocalDateTime.now();

        var registerSession = SignalSession.builder()
                .id(generateId())
                .createdAt(now)
                .expiredAt(now.plusHours(DEFAULT_SESSION_HOURS))
                .dbName(dbInfo.name())
                .dbVersion(dbInfo.version())
                .recordName(record.getName())
                .signalInput(signalInput)
                .build();

        sessions.put(registerSession.getId(), registerSession);
        return registerSession;
    }
}







































