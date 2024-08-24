package io.graphys.wfdbjstore.engine.util;

public class IdUtils {
    public static String formDatabaseId(String dbName, String dbVersion) {
        return dbName + "|" + dbVersion;
    }
}
