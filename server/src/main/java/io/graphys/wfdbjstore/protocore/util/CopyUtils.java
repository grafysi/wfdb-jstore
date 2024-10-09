package io.graphys.wfdbjstore.protocore.util;

import io.graphys.wfdbjstore.engine.metadataquery.PathInfo;
import io.graphys.wfdbjstore.engine.metadataquery.Record;
import io.graphys.wfdbjstore.protocol.content.PathInfoReadContent;
import io.graphys.wfdbjstore.protocol.content.RecordReadContent;

import java.util.Arrays;

public class CopyUtils {
    public static PathInfoReadContent copyFrom(PathInfo pathInfo) {
        return PathInfoReadContent
                .builder()
                .recordName(pathInfo.getRecordName())
                .pathSegments(pathInfo.getPathSegments())
                .build();
    }

    public static RecordReadContent.SignalInfo copyFrom(Record.SignalInfo si) {
        if (si == null) return null;
        return RecordReadContent.SignalInfo
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

    public static RecordReadContent copyFrom(Record record) {
        if (record == null) return null;
        return RecordReadContent
                .builder()
                .name(record.getName())
                .totalSamples(record.getTotalSamples())
                .signalInfo(
                        Arrays
                                .stream(record.getSignalInfo())
                                .map(CopyUtils::copyFrom)
                                .toArray(RecordReadContent.SignalInfo[]::new))
                .sampleFrequency(record.getSampleFrequency())
                .baseTime(record.getBaseTime())
                .textInfo(record.getTextInfo())
                .build();
    }
}
