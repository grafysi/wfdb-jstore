package io.graphys.wfdbjstore.recordstore.header;

import io.graphys.wfdbjstore.recordstore.SegmentInfo;
import io.graphys.wfdbjstore.recordstore.SignalInfo;

public interface HeaderReader {
    boolean isMultiSegmentRecord(String recordPath);

    SegmentInfo[] readSegmentInfo(String recordPath);

    SignalInfo[] readSignalInfo(String recordPath);

    ReadHeader readFullHeader(String recordPath);
}
