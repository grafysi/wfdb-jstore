package io.graphys.wfdbjstore.engine;

import io.graphys.wfdbjstore.engine.session.MetadataSession;
import io.graphys.wfdbjstore.engine.session.SessionManager;
import io.graphys.wfdbjstore.engine.util.IdUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.Closeable;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class MetadataQueryExecutor extends SessionBasedExecutor<MetadataQuery<?>, MetadataSession> implements Closeable {

    private static final Logger logger = LogManager.getLogger(MetadataQueryExecutor.class);

    private final Map<String, MetadataQueryCacheRunner> dbRunners = Collections.synchronizedMap(new HashMap<>());
    private final ExecutionContext executionContext;
    private final ExecutorService executorService = Executors.newVirtualThreadPerTaskExecutor();


    public MetadataQueryExecutor(SessionManager<MetadataSession> sessionManager, ExecutionContext executionContext) {
        super(sessionManager);
        this.executionContext = executionContext;
    }

    @Override
    public void submit(MetadataQuery<?> execution, MetadataSession session) {
        var dbId = IdUtils.formDatabaseId(session.getDbName(), session.getDbVersion());
        if (!dbRunners.containsKey(dbId)) {
            var runner = new MetadataQueryCacheRunner();
            dbRunners.put(dbId, runner);
            executorService.submit(() -> executionContext.startMetadataQueryContext(session.getDbName(), session.getDbVersion(), runner));
        }
        dbRunners.get(dbId).putQuery(execution);
    }

    @Override
    public void close() {
        for (var runner: dbRunners.values()) {
            runner.close();
        }
        executorService.close();
    }

    @Override
    public Class<? extends WfdbExecution> getExecutionClass() {
        return MetadataQuery.class;
    }
}
