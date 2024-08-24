package io.graphys.wfdbjstore.recordstore.codec;

import java.io.Closeable;
import java.io.IOException;

public interface FlacPlayer extends Closeable {
    public enum FlatteningMethod {
        AVERAGING,
        SUMMING
    }

    long getSampleNumber();

    int[] nextSamples() throws IOException;

    void seek(long absSampleNumber) throws IOException;

    long getTotalSamples();

    int getFreqCoefficient();

    FlatteningMethod getFlatteningMethod();

    void initialize() throws IOException;
}
