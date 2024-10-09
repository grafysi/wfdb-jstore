package io.graphys.wfdbjstore.protocol.content;

import lombok.Builder;
import lombok.extern.jackson.Jacksonized;

import java.util.Arrays;

@Jacksonized
@Builder
public record ReadSignalFlowContent(Long sampleNumber, int[] samples) implements Content {
    @Override
    public String toString() {
        return String.format("%s[sampleNumber=%d, samples=%s]", getClass().getSimpleName(), sampleNumber, Arrays.toString(samples));
    }
}
