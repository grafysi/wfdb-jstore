package io.graphys.wfdbjstore.recordstore;

import java.io.Closeable;
import java.io.IOException;

public interface SignalInput extends Closeable {

    int[] readSamples() throws IOException;

    double[] readAnalogValues() throws IOException;

    long getTotalSamples();

    long getSampleNumber();

    long availSamples();

    void seek(long absSampleNumber) throws IOException;
}
