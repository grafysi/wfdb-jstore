package io.graphys.wfdbjstore.protocol.content;

import lombok.Builder;
import lombok.extern.jackson.Jacksonized;

@Builder
@Jacksonized
public record PathInfoReadContent(String recordName, String[] pathSegments) implements Content {

}
