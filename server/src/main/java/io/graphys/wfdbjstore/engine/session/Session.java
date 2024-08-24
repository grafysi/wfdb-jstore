package io.graphys.wfdbjstore.engine.session;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@AllArgsConstructor
public abstract sealed class Session permits MetadataSession, SignalSession {
    private String id;
    private LocalDateTime createdAt;
    private LocalDateTime expiredAt;
    private String dbName;
    private String dbVersion;
}
