package io.graphys.wfdbjstore.protocol.description;

import lombok.Builder;
import lombok.extern.jackson.Jacksonized;

@Builder
@Jacksonized
public record RecordReadDescription(Integer limit, String name, String[] textInfoPatterns) implements Description {
}









