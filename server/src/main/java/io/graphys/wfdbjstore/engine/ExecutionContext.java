package io.graphys.wfdbjstore.engine;

import io.graphys.wfdbjstore.engine.metadataquery.Metadata;
import io.graphys.wfdbjstore.engine.metadataquery.MetadataRepository;
import io.graphys.wfdbjstore.engine.metadataquery.Record;
import io.graphys.wfdbjstore.engine.metadataquery.RecordRepository;
import io.graphys.wfdbjstore.engine.session.*;
import io.graphys.wfdbjstore.engine.util.IdUtils;
import io.graphys.wfdbjstore.recordstore.SignalInput;
import io.graphys.wfdbjstore.recordstore.WfdbManager;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.*;


public final class ExecutionContext {

    private static final Logger logger = LogManager.getLogger(ExecutionContext.class);

    public static final String SIGNAL_INPUT_DEPENDENCY = "SIGNAL_INPUT_DEPENDENCY";

    private final Map<Class<? extends Session>, SessionManager<? extends Session>>
            classSessionManagerMap = Collections.synchronizedMap(new HashMap<>());

    private static final ScopedValue<Map<Class<? extends Session>, SessionManager<? extends Session>>>
            SCOPED_CLASS_SESSION_MANAGER_MAP = ScopedValue.newInstance();

    private final Map<String, Map<Class<? extends Metadata>, MetadataRepository<? extends Metadata>>>
            dbClassMetadataRepoMap = Collections.synchronizedMap(new HashMap<>());

    private static final ScopedValue<Map<Class<? extends Metadata>, MetadataRepository<? extends Metadata>>>
            SCOPED_CLASS_METADATA_REPO_MAP = ScopedValue.newInstance();

    private static final ScopedValue<SignalInput> SCOPED_SIGNAL_INPUT = ScopedValue.newInstance();

    //private static final ScopedValue<SessionRegistry<?>> SESSION_REGISTRY = ScopedValue.newInstance();
    //private static final ScopedValue<MetadataRepository<?>> METADATA_REPO = ScopedValue.newInstance();

    private final WfdbManager wfdbManager;
    private final AuthenticationManager authManager;

    public ExecutionContext(WfdbManager wfdbManager, AuthenticationManager authManager) {
        this.wfdbManager = wfdbManager;
        this.authManager = authManager;
        initSessionManagers();
        initMetadataRepositories();
    }

    public<T extends Session> SessionManager<T> getSessionManager(Class<T> sessionClass) {
        //noinspection unchecked
        return (SessionManager<T>) classSessionManagerMap.get(sessionClass);
    }


    private void initSessionManagers() {
        /*sessionManagers.put(MetadataSession.class,
                new MetadataSessionManager(authManager, wfdbManager.getWfdbStores(), wfdbManager.getRecordRepository()));*/
        classSessionManagerMap.put(MetadataSession.class,
                new MetadataSessionManager(authManager, wfdbManager.getWfdbStores(), wfdbManager.getRecordRepository()));

        classSessionManagerMap.put(SignalSession.class,
                new SignalSessionManager(authManager, wfdbManager.getWfdbStores(), wfdbManager.getRecordRepository()));
    }

    private void initMetadataRepositories() {
        // init for record type only
        var recordRepos = new HashMap<Class<?>, MetadataRepository<?>>();
        //dbClassMetadataRepoMap.put(Record.class, recordRepos);
        for (var wfdbStore: wfdbManager.getWfdbStores()) {
            /*recordRepos.put(IdUtils.formDatabaseId(wfdbStore.getDbInfo().name(), wfdbStore.getDbInfo().version()),
                    new RecordRepository(wfdbStore, wfdbManager.getRecordRepository()));*/
            var dbInfo = wfdbStore.getDbInfo();
            var classMetadataRepoMap = new HashMap<Class<? extends Metadata>, MetadataRepository<? extends Metadata>>();
            classMetadataRepoMap.put(Record.class, new RecordRepository(wfdbStore, wfdbManager.getRecordRepository()));
            dbClassMetadataRepoMap.put(IdUtils.formDatabaseId(dbInfo.name(), dbInfo.version()), classMetadataRepoMap);
        }
    }

    static <T extends Session> SessionRegistry<T> getSessionRegistry(Class<T> sessionClass) throws ContextualAccessException {
        try {
            //noinspection unchecked
            return (SessionRegistry<T>) SCOPED_CLASS_SESSION_MANAGER_MAP.get().get(sessionClass);
            //noinspection
            //return (SessionManager<T>) SESSION_REGISTRY.get();
        } catch (Exception e) {
            throw new ContextualAccessException(e);
        }
    }

    static <T extends Metadata> MetadataRepository<T> getMetadataRepository(Class<T> metadataClass) throws ContextualAccessException {
        try {

            //noinspection unchecked
            var result = (MetadataRepository<T>) SCOPED_CLASS_METADATA_REPO_MAP.get().get(metadataClass);
            if (result == null) {
                throw new IllegalStateException("SCOPED_CLASS_METADATA_REPO_MAP does not initialized correctly.");
            }
            return result;
            //noinspection
            //return (MetadataRepository<T>) METADATA_REPO.get();
        } catch (Exception e) {
            throw new ContextualAccessException(e);
        }
    }

    static SignalInput getSignalInput() throws ContextualAccessException {
        try {
            return SCOPED_SIGNAL_INPUT.get();
        } catch (Exception e) {
            throw new ContextualAccessException(e);
        }
    }

    public void startSessionInitializationContext(Runnable op) {
        ScopedValue.runWhere(SCOPED_CLASS_SESSION_MANAGER_MAP, classSessionManagerMap, op);
    }

    public void startMetadataQueryContext(String dbName, String dbVersion, Runnable op) {
        var repos = dbClassMetadataRepoMap.get(IdUtils.formDatabaseId(dbName, dbVersion));
        ScopedValue.runWhere(SCOPED_CLASS_METADATA_REPO_MAP, repos, op);
    }

    public void startSessionContext(String sessionId, String dependency, Runnable op) {
        switch (dependency) {
            case SIGNAL_INPUT_DEPENDENCY -> {
                ScopedValue.runWhere(SCOPED_SIGNAL_INPUT, resolveSignalInput(sessionId), op);
            }
            case null, default -> {
                throw new RuntimeException("Unexpected runtime behaviour");
            }
        }
    }

    private SignalInput resolveSignalInput(String sessionId) {

        var signalSessionManager = getSessionManager(SignalSession.class);
        var signalSession = signalSessionManager.getSession(sessionId);

        Objects.requireNonNull(signalSession);
        return signalSession.getSignalInput();
    }
}














































