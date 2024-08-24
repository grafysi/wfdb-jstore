package io.graphys.wfdbjstore.engine.session;

import io.graphys.wfdbjstore.engine.session.auth.AuthToken;
import io.graphys.wfdbjstore.engine.session.auth.AuthenticationException;
import io.graphys.wfdbjstore.recordstore.DatabaseInfo;
import io.graphys.wfdbjstore.recordstore.Record;
import io.graphys.wfdbjstore.recordstore.RecordRepository;
import io.graphys.wfdbjstore.recordstore.WfdbStore;

import java.util.Comparator;
import java.util.List;
import java.util.UUID;

public sealed abstract class SessionManager<T extends Session> implements SessionRegistry<T>
        permits MetadataSessionManager, SignalSessionManager {

    public static final int DEFAULT_SESSION_HOURS = 48;
    private final AuthenticationManager authManager;
    private final List<WfdbStore> wfdbStores;
    private final RecordRepository recordRepo;

    public SessionManager(AuthenticationManager authManager, List<WfdbStore> wfdbStores, RecordRepository recordRepo) {
        this.authManager = authManager;
        this.wfdbStores = wfdbStores;
        this.recordRepo = recordRepo;
    }

    public abstract T getSession(String id);

    protected final void authenticatedOrElseThrows(AuthToken token) throws AuthenticationException {
        if (!authManager.authenticate(token)) {
            throw new AuthenticationException("Invalid credentials.");
        }
    }

    protected String generateId() {
        return UUID.randomUUID().toString();
    }

    protected final DatabaseInfo getDatabase(String dbName, String version) {
        if (dbName == null) {
            return null;
        }

        if (version == null) {
            return wfdbStores
                    .stream()
                    .map(WfdbStore::getDbInfo)
                    .filter(db -> db.name().equals(dbName))
                    .sorted(Comparator.comparing(DatabaseInfo::version))
                    .toList()
                    .getLast();
        }

        return wfdbStores
                .stream()
                .map(WfdbStore::getDbInfo)
                .filter(db -> db.name().equals(dbName) && db.version().equals(version))
                .findFirst()
                .orElse(null);
    }

    protected final Record getRecord(String name, DatabaseInfo dbInfo) {
        var wfdbStore = wfdbStores
                .stream()
                .filter(s -> s.getDbInfo().equals(dbInfo))
                .findFirst()
                .orElse(null);

        if (wfdbStore == null) {
            return null;
        }

        var pathInfo = wfdbStore.findPathInfoOf(name);

        if (pathInfo == null) {
            return null;
        }

        return recordRepo.findBy(pathInfo);
    }
}
