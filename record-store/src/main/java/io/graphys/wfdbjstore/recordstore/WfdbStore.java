package io.graphys.wfdbjstore.recordstore;

public interface WfdbStore {
    Skeleton getSkeleton();

    PathInfo findPathInfoOf(String recordName);

    PathInfo[] findAllPathInfo();

    String[] findPathSegments(int ordinal);

    boolean isBuilt();

    void build();

    DatabaseInfo getDbInfo();
}
