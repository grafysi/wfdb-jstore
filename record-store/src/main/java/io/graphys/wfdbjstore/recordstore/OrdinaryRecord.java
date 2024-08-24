package io.graphys.wfdbjstore.recordstore;

import io.graphys.wfdbjstore.recordstore.codec.FlacPlayer;
import io.graphys.wfdbjstore.recordstore.codec.FlacPlayerImpl;
import io.graphys.wfdbjstore.recordstore.exception.SignalDataPrepareFailedException;
import lombok.Builder;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.io.InputStream;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class OrdinaryRecord implements Record {
    private static final Logger logger = LogManager.getLogger(OrdinaryRecord.class);

    protected final PathInfo pathInfo;
    protected final SignalInfo[] signalInfo;
    protected final double sampleFrequency;
    protected final LocalDateTime baseTime;
    protected final String[] textInfo;


    @Builder
    public OrdinaryRecord(PathInfo pathInfo, SignalInfo[] signalInfo, String[] textInfo, LocalDateTime baseTime, double sampFreq) {
        this.pathInfo = pathInfo;
        this.signalInfo = signalInfo;
        this.textInfo = textInfo;
        this.baseTime = baseTime;
        this.sampleFrequency = sampFreq;
    }

    @Override
    public PathInfo getPathInfo() {
        return pathInfo;
    }

    @Override
    public long getTotalSamples() {
        return signalInfo[0].getNumSamples();
    }

    @Override
    public LocalDateTime getBaseTime() {
        return baseTime;
    }

    @Override
    public double getSampleFrequency() {
        return sampleFrequency;
    }


    @Override
    public SignalInfo[] getSignalInfo() {
        return signalInfo;
    }

    @Override
    public String getName() {
        return pathInfo.getRecordName();
    }

    @Override
    public String[] getTextInfo() {
        return textInfo;
    }

    /*@Override
    public String toString() {
        return String.format(
                "Record name: %s%nSignal Info:%n%s",
                getName(),
                Arrays
                        .stream(signalInfo)
                        .map(Object::toString)
                        .collect(Collectors.joining("\n"))
                );
    }*/

    @Override
    public SignalInput newSignalInput() {
        return new SignalInputImpl();
    }

    SignalInput newInitializedSignalInput() {
        var si = new SignalInputImpl();
        Arrays
                .stream(si.flacPlayers)
                .forEach(fp -> {
                    try {
                        fp.initialize();
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                });
        return si;
    }

    private InputStream getSignalInputStream(String fileName) {
        try {
            return StorageContext.get().prepareSignal(fileName, pathInfo);
        } catch (IOException e) {
            throw new SignalDataPrepareFailedException("Cannot get input stream from storage context", e);
        }
    }

    public class SignalInputImpl implements SignalInput {
        private long sampleNumber = 0;
        private final long totalSamples;
        private final FlacPlayer[] flacPlayers;

        private SignalInputImpl() {
            flacPlayers = Arrays.stream(signalInfo)
                    .collect(Collectors.groupingBy(
                            SignalInfo::getFileName,
                            LinkedHashMap::new,
                            Collectors.mapping(SignalInfo::getSpf, Collectors.toList()))
                    )
                    .sequencedEntrySet()
                    .stream()
                    .map(s -> new FlacPlayerImpl(
                                getSignalInputStream(s.getKey()),
                                s.getValue().getFirst(),
                                FlacPlayer.FlatteningMethod.AVERAGING)
                    )
                    .toArray(FlacPlayer[]::new);

            /*totalSamples = Arrays.stream(signalInfo)
                    .mapToLong(SignalInfo::getNumSamples)
                    .sum();*/
            totalSamples = signalInfo[0].getNumSamples();
        }

        @Override
        public void close() throws IOException {
            for (var player: flacPlayers) {
                player.close();
            }
        }

        @Override
        public void seek(long absSampleNumber) throws IOException {
            if (absSampleNumber < 0 || absSampleNumber > totalSamples) {
                throw new IOException("Invalid seek number: " + absSampleNumber + ":" + totalSamples);
            }
            for (var fp: flacPlayers) {
                fp.seek(absSampleNumber);
            }
            sampleNumber = absSampleNumber;
        }

        @Override
        public long getTotalSamples() {
            return totalSamples;
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
        public int[] readSamples() throws IOException {
            try {
                /**
                 * toArray seem so slow, try to fix
                 */
                // previous code
                /*var result = Arrays
                        .stream(flacPlayers)
                        .map(fp -> {
                            try {
                                return fp.nextSamples();
                            } catch (IOException e) {
                                throw new RuntimeException(e);
                            }
                        })
                        .flatMap(s -> Arrays.stream(s).boxed())
                        .mapToInt(Integer::intValue)
                        .toArray();*/

                // new code #1
                /*var samples = new LinkedList<Integer>();
                Arrays
                        .stream(flacPlayers)
                        .map(fp -> {
                            try {
                                return fp.nextSamples();
                            } catch (IOException e) {
                                throw new RuntimeException(e);
                            }
                        })
                        .flatMap(s -> Arrays.stream(s).boxed())
                        .forEach(samples::add);
                var result = new int[samples.size()];
                for (int i = 0; i < result.length; i++) {
                    result[i] = samples.removeFirst();
                }*/

                var sampleChunks = new int[flacPlayers.length][];
                var nSamples = 0;
                for (int i = 0; i < flacPlayers.length; i++) {
                    var chunk = flacPlayers[i].nextSamples();
                    sampleChunks[i] = chunk;
                    nSamples += chunk.length;
                }
                var result = new int[nSamples];
                var startPos = 0;
                for (var chunk: sampleChunks) {
                    for (int i = 0; i < chunk.length; i++) {
                        result[startPos + i] = chunk[i];
                    }
                    startPos += chunk.length;
                }

                // un-touch
                sampleNumber++;
                return result;
            } catch (Exception e) {
                if (e.getCause() instanceof IOException rootCause) {
                    throw rootCause;
                }
                throw e;
            }
        }

        @Override
        public double[] readAnalogValues() throws IOException {
            var samples = readSamples();
            return IntStream
                    .range(0, samples.length)
                    .mapToDouble(i -> samples[i] / signalInfo[i].getGain())
                    .toArray();
        }
    }

    @Override
    public String toString() {
        var builder = new StringBuilder();

        builder
                .append("name: ").append(getName()).append("; ")
                .append("baseTime: ").append(baseTime).append("; ")
                .append("sampFreq: ").append(sampleFrequency).append("; ")
                .append("nSamples: ").append(getTotalSamples()).append("; ")
                .append("textInfo: ").append(String.join("|", textInfo)).append("; ")
                .append("signalInfo: ").append(
                        Arrays.stream(signalInfo).map(Objects::toString).collect(Collectors.joining("|"))
                );

        return builder.toString();
    }

}


















































