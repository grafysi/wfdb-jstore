package io.graphys.wfdbjstore.protocol.description;

import io.graphys.wfdbjstore.protocol.exchange.MediaType;
import lombok.Builder;
import lombok.extern.jackson.Jacksonized;

@Builder
@Jacksonized
public record SignalConnectionDescription(String scheme, String token, String dbName,
                                          String dbVersion, String recordName,
                                          MediaType reportMediaType, Boolean isReactive) implements Description {
}
