package io.graphys.wfdbjstore.protocol.description;

import lombok.Builder;
import lombok.extern.jackson.Jacksonized;

@Builder
@Jacksonized
public record ReadSignalFlowDescription(Long frameSkip, Long frameLimit) implements Description {

}
