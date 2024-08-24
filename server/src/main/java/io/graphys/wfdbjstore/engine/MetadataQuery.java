package io.graphys.wfdbjstore.engine;

import io.graphys.wfdbjstore.engine.metadataquery.Metadata;
import io.graphys.wfdbjstore.engine.metadataquery.MetadataRepository;

public interface MetadataQuery<T extends Metadata> extends WfdbExecution {

    default MetadataRepository<T> getMetadataRepository() throws ContextualAccessException {
        return ExecutionContext.getMetadataRepository(getResultClass());
    }

    public Class<T> getResultClass();

    default boolean shareResult(Object consumer) {
        return false;
    }

    default Object getResultConsumer() {
        return null;
    }
}
