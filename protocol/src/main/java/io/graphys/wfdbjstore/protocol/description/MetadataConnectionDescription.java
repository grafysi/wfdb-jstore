package io.graphys.wfdbjstore.protocol.description;

import io.graphys.wfdbjstore.protocol.exchange.MediaType;
import lombok.Builder;
import lombok.extern.jackson.Jacksonized;

@Builder
@Jacksonized
public record MetadataConnectionDescription(String scheme, String token, String dbName,
                                            String dbVersion, MediaType reportMediaType, Boolean isReactive) implements Description {
}
