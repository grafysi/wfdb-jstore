package io.graphys.wfdbjstore.recordstore;

import lombok.Builder;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.stream.IntStream;

public class MultiSegmentRecord extends OrdinaryRecord {
    private static final Logger logger = LogManager.getLogger(MultiSegmentRecord.class);
    private final SegmentInfo[] segmentInfo;
    private final SegmentedRecord[] segments;
    private final long totalSamples;

    @Builder(builderMethodName = "multiSegmentBuilder")
    public MultiSegmentRecord(PathInfo pathInfo, SignalInfo[] signalInfo, String[] textInfo, SegmentInfo[] segmentInfo,
                              LocalDateTime baseTime, double sampFreq, SegmentedRecord[] segments) {
        super(pathInfo, signalInfo, textInfo, baseTime, sampFreq);
        this.segmentInfo = segmentInfo;
        this.segments = segments;
        totalSamples = Arrays
                .stream(segments)
                .mapToLong(Record::getTotalSamples)
                .sum();
    }

    @Override
    public SignalInput newSignalInput() {
        return new MultiSegmentSignalInput();
    }

    @Override
    public long getTotalSamples() {
        return totalSamples;
    }

    public class MultiSegmentSignalInput implements SignalInput {
        private static final int PREPARE_NEXT = 3;
        private long sampleNumber = 0;
        private final long totalSamples;
        private final SignalInput[] signalInputs;
        private final SignalMap[] signalMaps;
        private int inputCurrent;

        private MultiSegmentSignalInput() {
            signalInputs = new SignalInput[segments.length];

            signalMaps = new SignalMap[segments.length];

            /*signalMaps = Arrays
                    .stream(segments)
                    .map(Record::getSignalInfo)
                    .map(this::createSignalMap)
                    .toArray(SignalMap[]::new);*/

            setInputCurrent(0);

            totalSamples = Arrays
                    .stream(segments)
                    .mapToLong(segment -> segment.getSignalInfo()[0].getNumSamples())
                    .sum();
        }

        private void setInputCurrent(int newValue) {
            inputCurrent = newValue;
            if (signalInputs[inputCurrent] == null) {
                prepareSignalInput(inputCurrent, true);
            }
            for (int i = inputCurrent + 1; i < Math.min(inputCurrent + 1 + PREPARE_NEXT, signalInputs.length); i++) {
                if (signalInputs[i] == null) {
                    prepareSignalInput(i, false);
                }
            }
        }

        @Override
        public void close() throws IOException {
            for (var si: signalInputs) {
                if (si != null) {
                    si.close();
                }
            }
        }

        @Override
        public void seek(long absSampleNumber) throws IOException {
            if (absSampleNumber < 0 || absSampleNumber > totalSamples) {
                throw new IOException("Invalid seek number: " + absSampleNumber);
            }

            long passedSamples = 0;
            int siIdx = 0;
            while (passedSamples + segments[siIdx].getTotalSamples() <= absSampleNumber) {
                passedSamples += segments[siIdx].getTotalSamples();
                siIdx++;
            }
            setInputCurrent(siIdx);
            logger.info("Call seek: inputCurrent={}, skip next samples {}, recname: {}",
                    inputCurrent, absSampleNumber - passedSamples, segments[inputCurrent].getName());
            signalInputs[siIdx].seek(absSampleNumber - passedSamples);
            sampleNumber = absSampleNumber;
        }

        @Override
        public long getTotalSamples() {
            return Arrays
                    .stream(segments)
                    .mapToLong(seg -> seg.getSignalInfo()[0].getNumSamples())
                    .sum();
        }

        @Override
        public long getSampleNumber() {
            return sampleNumber;
        }

        @Override
        public long availSamples() {
            return totalSamples - sampleNumber;
        }

        @Override
        public int[] readSamples() throws IOException{
            /*if (signalInputs[inputCurrent] == null) {
                prepareSignalInput(inputCurrent, true);

                for (int i = inputCurrent + 1; i < Math.min(inputCurrent + 1 + PREPARE_NEXT, segments.length); i++) {
                    prepareSignalInput(i, false);
                }
            }*/
            if (signalInputs[inputCurrent].availSamples() <= 0) {
                /*++inputCurrent;
                if (inputCurrent + PREPARE_NEXT < segments.length) {
                    prepareSignalInput(inputCurrent + PREPARE_NEXT, false);
                }*/
                setInputCurrent(inputCurrent + 1);
            }
            var samples = signalInputs[inputCurrent].readSamples();
            sampleNumber++;
            return signalMaps[inputCurrent].mapFrom(samples);
        }

        @Override
        public double[] readAnalogValues() throws IOException{
            var samples = this.readSamples();
            return IntStream
                    .range(0, samples.length)
                    .mapToDouble(i -> samples[i] / signalInfo[i].getGain())
                    .toArray();
        }

        private void prepareSignalInput(int position, boolean init) {
            var signalInput = init
                    ? segments[position].newInitializedSignalInput()
                    : segments[position].newSignalInput();
            signalInputs[position] = segments[position].newSignalInput();
            if (signalMaps[position] == null) {
                signalMaps[position] = createSignalMap(segments[position].getSignalInfo());
            }
        }

        private SignalMap createSignalMap(SignalInfo[] fromSi) {
            var positions = new int[fromSi.length];
            var multipliers = new double[fromSi.length];
            var shifters = new int[fromSi.length];
            var toSi = signalInfo;
            for (int fIdx = 0; fIdx < fromSi.length; fIdx++) {
                for (int tIdx = 0; tIdx < toSi.length; tIdx++) {
                    if (fromSi[fIdx].getDescription().equals(toSi[tIdx].getDescription())) {
                        positions[fIdx] = tIdx;
                        multipliers[fIdx] = toSi[tIdx].getGain() / fromSi[fIdx].getGain();
                        shifters[fIdx] = toSi[tIdx].getBaseline() - fromSi[fIdx].getBaseline();
                        break;
                    }
                }
            }
            return new SignalMap(toSi.length, positions, multipliers, shifters);
        }
    }

    private static class SignalMap {
        private static final int INVALID_VALUE = -32768;
        private final int totalPositions;
        private final int[] positions;

        private final double[] multipliers;

        private final int[] shifters;

        public SignalMap(int totalPositions, int[] positions, double[] multipliers, int[] shifters) {
            this.positions = positions;
            this.multipliers = multipliers;
            this.shifters = shifters;
            this.totalPositions = totalPositions;
        }

        private int[] mapFrom(int[] fromSamples) {
            var toSamples = new int[totalPositions];
            Arrays.fill(toSamples, INVALID_VALUE);
            for (int i = 0; i < positions.length; i++) {
                if (fromSamples[i] != INVALID_VALUE) {
                    toSamples[positions[i]] = (int) Math.round(fromSamples[i] * multipliers[i]) + shifters[i];
                }
            }
            return toSamples;
        }

        @Override
        public String toString() {
            return String.format("pos: %s; mul: %s; shf: %s",
                    Arrays.toString(positions),
                    Arrays.toString(multipliers),
                    Arrays.toString(shifters));
        }

    }
}































































