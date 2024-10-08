package io.graphys.wfdbjstore.recordstore.header;

import java.util.LinkedList;
import java.util.concurrent.locks.ReentrantLock;
import java.util.stream.IntStream;

import io.graphys.wfdbjstore.recordstore.SegmentInfo;
import io.graphys.wfdbjstore.recordstore.SignalInfo;
import io.graphys.wfdbjstore.recordstore.transaction.NativeTransaction;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import wfdb.*;

public class NativeHeaderReader implements HeaderReader {
    private static final Logger logger = LogManager.getLogger(NativeHeaderReader.class);
    private ReentrantLock lock = new ReentrantLock();

    public NativeHeaderReader() {
    }

    @Override
    public boolean isMultiSegmentRecord(String recordPath) {

        return NativeTransaction.startTransaction(() -> {
            wfdb.isigopen(recordPath, null, 0);
            var result = wfdb.getseginfocount() > 0;
            wfdb.wfdbquit();
            return result;
        });
    }

    @Override
    public SignalInfo[] readSignalInfo(String recordPath) {
        return NativeTransaction.startTransaction(() -> {
            var nSig = wfdb.isigopen(recordPath, null, 0);
            var sia = new WFDB_SiginfoArray(nSig);
            wfdb.isigopen(recordPath, sia.cast(), -nSig);
            var result = IntStream
                    .range(0, nSig)
                    .mapToObj(sia::getitem)
                    .map(SignalInfo::from)
                    .toArray(SignalInfo[]::new);
            wfdb.wfdbquit();
            return result;
        });
    }

    @Override
    public SegmentInfo[] readSegmentInfo(String recordPath) {
        return NativeTransaction.startTransaction(() -> {
            wfdb.isigopen(recordPath, null, 0);
            var nSeg = wfdb.getseginfocount();
            var seiArray = new WFDB_SeginfoArray(wfdb.getseginfoptr(), false);

            /**
             * re-implement the section with toArray
             */
            // old section
            var result = IntStream
                    .range(0, nSeg)
                    .mapToObj(seiArray::getitem)
                    .map(SegmentInfo::from)
                    .toArray(SegmentInfo[]::new);

            // new section #1
            /*var result = new SegmentInfo[nSeg];
            for (int i = 0; i < nSeg; i++) {
                var seg = seiArray.getitem(i);
                result[i] = SegmentInfo.from(seg);
                seg.delete();
            }*/

            // un-touch
            seiArray.delete();


            wfdb.wfdbquit();
            return result;
        });
    }

    @Override
    public ReadHeader readFullHeader(String recordPath) {
        return NativeTransaction.startTransaction(() -> {
            // open signal
            var nSig = wfdb.isigopen(recordPath, null, 0   );

            //  read signal info
            var siArray = new WFDB_SiginfoArray(nSig);
            wfdb.isigopen(recordPath, siArray.cast(), -nSig);

            /**
             * As toArray to costly,
             * reimplement this section of find signal info
             */

            // old section
            var signalInfo = IntStream
                    .range(0, nSig)
                    .mapToObj(siArray::getitem)
                    .map(SignalInfo::from)
                    .toArray(SignalInfo[]::new);
            siArray.delete();

            // new section #1
            /*var signalInfo = new SignalInfo[nSig];
            for (int i = 0; i < nSig; i++) {
                var si = siArray.getitem(i);
                signalInfo[i] = SignalInfo.from(si);
                si.delete();
            }
            siArray.delete();*/

            // read base time
            var baseTimeStr = wfdb.timstr(0);

            // read sample freq
            var sampFreq = wfdb.getifreq();

            // read info list
            var infoList = new LinkedList<String>();
            String info = null;
            while ((info = wfdb.getinfo(null)) != null) {
                infoList.add(info);
            }

            // read segment info
            var nSeg = wfdb.getseginfocount();
            SegmentInfo[] segmentInfo = null;
            if (nSeg > 0) {
                var seiArray = new WFDB_SeginfoArray(wfdb.getseginfoptr(), false);
                /**
                 * reimplement the toArray section
                 */

                // old section
                segmentInfo = IntStream
                        .range(0, nSeg)
                        .mapToObj(seiArray::getitem)
                        .map(SegmentInfo::from)
                        .toArray(SegmentInfo[]::new);

                // new section #1
                /*segmentInfo = new SegmentInfo[nSeg];
                for (int i = 0; i < nSeg; i++) {
                    var seg = seiArray.getitem(i);
                    segmentInfo[i] = SegmentInfo.from(seg);
                    seg.delete();
                }*/

                // un-touch
                seiArray.delete();
            }

            var header = ReadHeader
                    .builder()
                    .signalInfo(signalInfo)
                    .segmentInfo(segmentInfo)
                    .baseTimeStr(baseTimeStr)
                    .sampFreq(sampFreq)
                    .infoList(infoList)
                    .build();
            wfdb.wfdbquit();
            return header;
        });
    }
}











