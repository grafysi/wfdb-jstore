package io.graphys.wfdbjstore.protocol.content;

import lombok.Builder;
import lombok.ToString;
import lombok.extern.jackson.Jacksonized;

import java.time.LocalDateTime;

@Builder
@Jacksonized
public record RecordReadContent(String name, SignalInfo[] signalInfo, Double sampleFrequency, LocalDateTime baseTime, String[] textInfo) implements Content {

    @Builder
    @Jacksonized
    public record SignalInfo(String fileName, String description, String units, Double gain, Integer initValue,
                             Long group, Integer formatCode, Integer spf, Integer blockSize, Integer adcResolution, Integer adcZero, Integer baseline, Long numSamples) {
    }
}








































