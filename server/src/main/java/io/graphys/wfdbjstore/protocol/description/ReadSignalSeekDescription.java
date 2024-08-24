package io.graphys.wfdbjstore.protocol.description;

import lombok.Builder;
import lombok.extern.jackson.Jacksonized;

@Builder
@Jacksonized
public record ReadSignalSeekDescription(Long seekNumber) implements Description {

}
