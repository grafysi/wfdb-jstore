package io.graphys.wfdbjstore.recordstore;

public interface Skeleton {
    public void build();

    public boolean isBuilt();

    public PathInfo[] findAllPathInfo();

    public String[] findPathSegments(int  ordinal);

    public PathInfo findPathInfoOf(String recordName);
}
