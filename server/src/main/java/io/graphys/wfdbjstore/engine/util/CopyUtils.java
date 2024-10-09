package io.graphys.wfdbjstore.engine.util;

import io.graphys.wfdbjstore.engine.metadataquery.PathInfo;
import io.graphys.wfdbjstore.engine.metadataquery.Record;
import io.graphys.wfdbjstore.recordstore.SignalInfo;

import java.util.Arrays;

public class CopyUtils {
    public static PathInfo copyFrom(io.graphys.wfdbjstore.recordstore.PathInfo pathInfo) {
        return PathInfo
                .builder()
                .recordName(pathInfo.getRecordName())
                .pathSegments(pathInfo.getPathSegments())
                .build();
    }

    public static Record.SignalInfo copyFrom(SignalInfo si) {
        if (si == null) return null;
        return Record.SignalInfo
                .builder()
                .fileName(si.getFileName())
                .gain(si.getGain())
                .group(si.getGroup())
                .spf(si.getSpf())
                .adcZero(si.getAdcZero())
                .units(si.getUnits())
                .formatCode(si.getFormatCode())
                .initValue(si.getInitValue())
                .numSamples(si.getNumSamples())
                .baseline(si.getBaseline())
                .blockSize(si.getBlockSize())
                .description(si.getDescription())
                .adcResolution(si.getAdcResolution())
                .build();
    }

    public static Record copyFrom(io.graphys.wfdbjstore.recordstore.Record record) {
        if (record == null) return null;
        return Record
                .builder()
                .name(record.getName())
                .totalSamples(record.getTotalSamples())
                .signalInfo(
                        Arrays
                                .stream(record.getSignalInfo())
                                .map(CopyUtils::copyFrom)
                                .toArray(Record.SignalInfo[]::new))
                .sampleFrequency(record.getSampleFrequency())
                .baseTime(record.getBaseTime())
                .textInfo(record.getTextInfo())
                .build();
    }
}
