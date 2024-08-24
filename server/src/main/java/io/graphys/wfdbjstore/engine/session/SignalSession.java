package io.graphys.wfdbjstore.engine.session;

import io.graphys.wfdbjstore.recordstore.SignalInput;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
public final class SignalSession extends Session {

    private SignalInput signalInput;

    private String recordName;

    @Builder
    public SignalSession(String id, LocalDateTime createdAt,
                         LocalDateTime expiredAt, String dbName, String dbVersion,
                         String recordName, SignalInput signalInput) {
        super(id, createdAt, expiredAt, dbName, dbVersion);
        this.recordName = recordName;
        this.signalInput = signalInput;
    }
}
