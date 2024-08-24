package io.graphys.wfdbjstore.recordstore;

import java.time.LocalDateTime;

public interface Record {
    SignalInfo[] getSignalInfo();

    String getName();

    LocalDateTime getBaseTime();

    String[] getTextInfo();

    double getSampleFrequency();

    SignalInput newSignalInput();

    long getTotalSamples();

    PathInfo getPathInfo();
}
