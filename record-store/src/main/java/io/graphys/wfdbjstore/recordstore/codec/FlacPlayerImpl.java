package io.graphys.wfdbjstore.recordstore.codec;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.kc7bfi.jflac.ChannelData;
import org.kc7bfi.jflac.FLACDecoder;
import org.kc7bfi.jflac.metadata.StreamInfo;

import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.Objects;
import java.util.stream.IntStream;
import java.util.stream.Stream;

public class FlacPlayerImpl implements FlacPlayer {

    private static final Logger logger = LogManager.getLogger(FlacPlayerImpl.class);
    private FLACDecoder decoder; // the main decoder to work with the flac file
    private StreamInfo streamInfo; // info of the flac data stream
    private long sampleNumber = 0; // the number of samples have been read
    private int framePosition = 0; // the number of sample have been read from the start of current frame
    private int frameSize = 0;
    private final int freqCoefficient;
    private final FlatteningMethod flatteningMethod;
    private final InputStream inStream;
    private boolean initialized = false;

    // new nChannel var introduce to avoid re-calculate nChannel each time call to nextSamples
    private int nChannels;


    public FlacPlayerImpl(InputStream is, int freqCoefficient, FlatteningMethod flatteningMethod) {
        this.freqCoefficient = freqCoefficient;
        this.flatteningMethod = flatteningMethod;
        this.inStream = is;
    }

    public boolean isInitialized() {
        return initialized;
    }

    public void initialize() throws IOException {
        this.decoder = new FLACDecoder(inStream);
        this.streamInfo = decoder.readStreamInfo();
        decoder.readMetadata(streamInfo);
        initialized = true;

        // because calculating of number channel is so costly, introduce new variable to count n channels
        nChannels = streamInfo.getChannels();
    }

    @Override
    public void close() throws IOException {
        inStream.close();
        decoder = null;
    }

    @Override
    public int getFreqCoefficient() {
        return freqCoefficient;
    }

    @Override
    public FlatteningMethod getFlatteningMethod() {
        return flatteningMethod;
    }

    @Override
    public long getSampleNumber() {
        return sampleNumber;
    }

    @Override
    public long getTotalSamples() {
        if (!initialized) {
            try {
                initialize();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
        return streamInfo.getTotalSamples() / freqCoefficient;
    }

    @Override
    public int[] nextSamples() throws IOException {
        if (!initialized) {
            //logger.info("init here #1");
            //var start = Instant.now();
            initialize();
            //logger.info("init here #2 (after {} ms)", Duration.between(start, Instant.now()).toMillis());
        }

        if (sampleNumber == getTotalSamples()) { // cut off residual part
            throw new IOException("exceed limit sample number:" + getTotalSamples() + ":" + sampleNumber);
        }

        if (frameSize == 0) {
            var frame = decoder.readNextFrame();
            framePosition = 0;
            frameSize = frame.header.blockSize;
        }

        var frameRemain = frameSize - framePosition;
        if (frameRemain == 0) {
            var frame = decoder.readNextFrame();
            framePosition = 0;
            frameSize = frame.header.blockSize;
        } else if (frameRemain < freqCoefficient) {
            var residualOutput = Arrays
                    .stream(decoder.getChannelData())
                    .filter(Objects::nonNull)
                    .map(ChannelData::getOutput)
                    .map(data -> Arrays.copyOfRange(data, framePosition, frameSize))
                    .toList();
            var frame = decoder.readNextFrame();
            frameSize = frame.header.blockSize;
            framePosition = 0;
            var nextOutput = Arrays
                    .stream(decoder.getChannelData())
                    .map(ChannelData::getOutput)
                    .map(data -> Arrays.copyOfRange(data, framePosition, framePosition + freqCoefficient - frameRemain))
                    .toList();

            framePosition += freqCoefficient - frameRemain;
            sampleNumber++;
            return IntStream
                    .range(0, residualOutput.size())
                    .mapToObj(i -> Stream
                            .concat(Arrays.stream(residualOutput.get(i)).boxed(), Arrays.stream(nextOutput.get(i)).boxed())
                            .mapToInt(Integer::intValue)
                            .toArray()
                    )
                    .mapToInt(this::flattenSamples)
                    .toArray();

        }
        var channelData = decoder.getChannelData();
        /**
         * toArray seem so costly,
         * try to fix
         */
        /*var result = Arrays
                .stream(channelData)
                .filter(Objects::nonNull)
                .map(ChannelData::getOutput)
                .map(data -> Arrays.copyOfRange(data, framePosition, framePosition + freqCoefficient))
                .mapToInt(this::flattenSamples)
                .toArray();*/

        /*var samples = Arrays
                .stream(channelData)
                .filter(Objects::nonNull)
                .map(ChannelData::getOutput)
                .map(data -> Arrays.copyOfRange(data, framePosition, framePosition + freqCoefficient))
                .map(this::flattenSamples)
                .toList();
        var result = samples.stream().mapToInt(Integer::intValue).toArray();*/

        // because introducing of new field nChannels, no need to re-calculate nChannels
        //var nChannels = (int) Arrays.stream(channelData).filter(Objects::nonNull).count();
        var result = new int[nChannels];
        for (int i = 0; i < nChannels; i++) {
            var data = channelData[i].getOutput();
            int processedSample;
            if (freqCoefficient > 1) {
                var bundle = Arrays.copyOfRange(data, framePosition, framePosition + freqCoefficient);
                processedSample = flattenSamples(bundle);
            } else {
                processedSample = data[framePosition];
            }
            result[i] = processedSample;
        }



        framePosition += freqCoefficient;
        sampleNumber++;
        return result;
    }

    @Override
    public void seek(long absSampleNumber) throws IOException {
        if (!initialized) {
            initialize();
        }
        if (absSampleNumber < 0 || absSampleNumber > getTotalSamples()) {
            throw new IOException("Invalid sample number: " + absSampleNumber);
        }
        var seekPoint = decoder.seek( absSampleNumber * freqCoefficient);

        sampleNumber = absSampleNumber;
        framePosition = seekPoint.getFrameSamples();
        frameSize = decoder.getChannelData()[0].getOutput().length;
    }

    private int flattenSamples(int[] samples) {
        /**
         * the calculating of AVERAGING is so slow,
         * so reimplement it
         */
        /*return switch (flatteningMethod) {
            case AVERAGING -> (int) Math.round(IntStream.of(samples).average().orElse(0));
            case SUMMING -> IntStream.of(samples).sum();
        };*/
        if (samples.length == 1) {
            return samples[0];
        }

        return switch (flatteningMethod) {
            case AVERAGING -> {
                int sum = 0;
                var sLength = samples.length;
                for (int sample : samples) {
                    sum += sample;
                }
                var avg = sum >= 0 ? (float) sum / sLength + 0.5 : (float) sum / sLength - 0.5;
                yield (int) avg;
            }
            case SUMMING -> IntStream.of(samples).sum();
        };
    }
}

































