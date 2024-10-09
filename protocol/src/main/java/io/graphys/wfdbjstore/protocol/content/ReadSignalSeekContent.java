package io.graphys.wfdbjstore.protocol.content;

import lombok.Builder;
import lombok.extern.jackson.Jacksonized;

@Builder
@Jacksonized
public record ReadSignalSeekContent(Long oldSampleNumber, Long newSampleNumber) implements Content {
}