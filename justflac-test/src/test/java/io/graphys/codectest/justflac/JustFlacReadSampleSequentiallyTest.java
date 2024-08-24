package io.graphys.codectest.justflac;

import org.junit.jupiter.api.Test;
import org.kc7bfi.jflac.ChannelData;
import org.kc7bfi.jflac.FLACDecoder;
import org.kc7bfi.jflac.io.RandomFileInputStream;
import org.kc7bfi.jflac.metadata.Metadata;

import java.io.IOException;
import java.util.Arrays;
import java.util.Objects;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

public class JustFlacReadSampleSequentiallyTest extends BaseTest {

    @Test
    public void testReadAndPrintFlacData() {
        var inName = "data/fin/83404654_0001e.dat";
        try (var inStream = new RandomFileInputStream(inName);
        ) {
            var decoder = new FLACDecoder(inStream);
            //decoder.readMetadata();
            var metadata = decoder.readNextMetadata();
            var frame = decoder.readNextFrame();

            Arrays
                    .stream(decoder.getChannelData())
                    .filter(Objects::nonNull)
                    .map(ChannelData::getOutput)
                    .map(data -> Arrays.stream(data).sum())
                    .forEach(logger::info);
        } catch (IOException e) {
            throw new RuntimeException("error", e);
        }
    }

    @Test
    public void testReadMetadata() {
        //var inName = "data/fin/83404654_0001e.dat";
        var inName = "data/fin/81739927_0001e.dat";
        try (var inStream = new RandomFileInputStream(inName)) {
            var decoder = new FLACDecoder(inStream);
            //var streamInfo = decoder.readStreamInfo();
            assertNotNull(decoder.getStreamInfo());
            var metadata = decoder.readMetadata();
            logger.info("Metadata array length: {}", metadata.length);
            Arrays
                    .stream(metadata)
                    .map(Metadata::getLength)
                    .forEach(logger::info);

        } catch (IOException e) {
            throw new RuntimeException("error", e);
        }
    }

    @Test
    public void testReadStreamInfo() {
        //var inName = "data/fin/83404654_0001e.dat";
        var inName = "data/fin/81739927_0008e.dat";
        try (var inStream = new RandomFileInputStream(inName)) {
            var decoder = new FLACDecoder(inStream);
            var streamInfo = decoder.readStreamInfo();
            logger.info(streamInfo);

        } catch (IOException e) {
            throw new RuntimeException("error", e);
        }
    }

    @Test
    public void testSampleNumberIndexing() {
        var inName = "data/fin/81739927_0008e.dat";
        try (var inStream = new RandomFileInputStream(inName)) {
            var decoder = new FLACDecoder(inStream);
            var streamInfo = decoder.readStreamInfo();
            decoder.readMetadata(streamInfo);
            var frame = decoder.readNextFrame();
            logger.info(frame.header);

        } catch (IOException e) {
            throw new RuntimeException("error", e);
        }
    }

    @Test
    public void testSkipSamples() {
        var inName = "data/fin/81739927_0008e.dat";
        try (var inStream = new RandomFileInputStream(inName)) {
            var decoder = new FLACDecoder(inStream);
            var streamInfo = decoder.readStreamInfo();
            decoder.readMetadata(streamInfo);

            for (int i = 0; i < 3; i++) {
                var frame = decoder.readNextFrame();
                var chanelData = decoder.getChannelData();
                var str =
                        Arrays
                                .stream(chanelData)
                                .filter(Objects::nonNull)
                                .map(ChannelData::getOutput)
                                .map(data -> Arrays.stream(data).sum())
                                .map(String::valueOf)
                                .collect(Collectors.joining(","));
                logger.info("The red line: ");
                logger.info(str);
                logger.info("frame-number: {}", frame.header.frameNumber);
                logger.info(frame.header);
            }
            var seekPoint = decoder.seek(5000);
            logger.info("seekPoint: {}", seekPoint);
            var chanelData = decoder.getChannelData();
            var str =
                    Arrays
                            .stream(chanelData)
                            .filter(Objects::nonNull)
                            .map(ChannelData::getOutput)
                            .map(data -> Arrays.stream(data).sum())
                            .map(String::valueOf)
                            .collect(Collectors.joining(","));
            logger.info("The red line: ");
            logger.info(str);
            var frame = decoder.readNextFrame();
            logger.info("frame-number: {}", frame.header.frameNumber);
            logger.info(frame.header);
            logger.info("samples decoded: {}", decoder.getSamplesDecoded());
        } catch (IOException e) {
            throw new RuntimeException("error", e);
        }
    }

    @Test
    void testReadChannelData() {
        var inName = "data/fin/81739927_0008e.dat";
        try (var inStream = new RandomFileInputStream(inName)) {
            var decoder = new FLACDecoder(inStream);
            var streamInfo = decoder.readStreamInfo();
            decoder.readMetadata(streamInfo);
            decoder.readNextFrame();
            assertNull(decoder.getChannelData());
        } catch (IOException e) {
            throw new RuntimeException("error", e);
        }
    }
}



























