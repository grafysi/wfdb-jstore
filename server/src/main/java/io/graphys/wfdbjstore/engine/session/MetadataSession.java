package io.graphys.wfdbjstore.engine.session;

import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
public final class MetadataSession extends Session {

    @Builder
    public MetadataSession(String id, LocalDateTime createdAt, LocalDateTime expiredAt, String dbName, String dbVersion) {
        super(id, createdAt, expiredAt, dbName, dbVersion);
    }
}
