package io.graphys.wfdbjstore.engine.metadataquery;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.jackson.Jacksonized;

import java.time.LocalDateTime;

@Builder
@Getter
public final class Record implements Metadata {
    private String name;
    private SignalInfo[] signalInfo;
    private double sampleFrequency;
    private LocalDateTime baseTime;
    private String[] textInfo;

    @Builder
    @Getter
    public static class SignalInfo {
        private String fileName;
        private String description;
        private String units;
        private double gain;
        private int initValue;
        private long group;
        private int formatCode;
        private int spf;
        private int blockSize;
        private int adcResolution;
        private int adcZero;
        private int baseline;
        private long numSamples;
    }
}