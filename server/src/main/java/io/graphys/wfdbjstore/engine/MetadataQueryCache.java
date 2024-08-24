package io.graphys.wfdbjstore.engine;

import io.graphys.wfdbjstore.engine.metadataquery.Metadata;

import java.util.List;

public class MetadataQueryCache {
    private final MetadataQuery<?> cachedQuery;

    public MetadataQueryCache(MetadataQuery<?> cachedQuery) {
        this.cachedQuery = cachedQuery;
    }

    public void caching(MetadataQuery<?> cachingQuery) throws QueryMismatchException {
        if (!cachedQuery.equals(cachingQuery)) {
            throw new QueryMismatchException("Two queries must be the equals.");
        }
        cachedQuery.shareResult(cachingQuery);
    }
}
