package io.graphys.wfdbjstore.protocol.content;

import lombok.Builder;
import lombok.extern.jackson.Jacksonized;

@Jacksonized
@Builder
public record ReadSignalFlowContent(Long sampleNumber, int[] samples) implements Content {

}
