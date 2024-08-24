package io.graphys.wfdbjstore.protocol.content;

import io.graphys.wfdbjstore.protocol.exchange.MediaType;
import lombok.Builder;
import lombok.extern.jackson.Jacksonized;

import java.time.LocalDateTime;

@Builder
@Jacksonized
public record MetadataConnectionContent(String sessionId, LocalDateTime createdAt, LocalDateTime expiredAt, String dbName,
                                        String dbVersion, MediaType reportMediaType, Boolean isReactive) implements Content {

}