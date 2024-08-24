package io.graphys.wfdbjstore.recordstore;

import io.graphys.wfdbjstore.recordstore.codec.FlacPlayer;
import io.graphys.wfdbjstore.recordstore.codec.FlacPlayerImpl;
import org.junit.jupiter.api.Test;
import org.kc7bfi.jflac.io.RandomFileInputStream;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.Arrays;
import java.util.stream.Collectors;

public class FlacPlayerImplTest extends BaseTest {

    @Test
    void testReadNextSample() {
        var inName = "data/input/81739927_0013e.dat";
        var outName = "data/output/console.csv";
        try (var inStream = new RandomFileInputStream(inName);
             var printWriter = new PrintWriter(outName)
        ) {
            var flacPlayer = new FlacPlayerImpl(inStream, 4, FlacPlayer.FlatteningMethod.AVERAGING);

            int[] samples;
            for(int i = 0; i < flacPlayer.getTotalSamples(); i++) {
                samples = flacPlayer.nextSamples();
                var row = Arrays
                        .stream(samples)
                        .mapToObj(String::valueOf)
                        .collect(Collectors.joining(","));
                printWriter.println(row);
            }

        } catch (IOException e) {
            throw new RuntimeException("error", e);
        }
    }

    @Test
    void testSeekSamples() {
        var inName = "data/input/81739927_0001e.dat";
        var outName = "data/output/console.csv";
        try (var inStream = new RandomFileInputStream(inName);
             var out = new PrintWriter(outName)
        ) {
            var flacPlayer = new FlacPlayerImpl(inStream, 4, FlacPlayer.FlatteningMethod.AVERAGING);
            int seekNumber = 285;
            flacPlayer.seek(seekNumber);
            int[] samples;
            for (int i = 0; i < flacPlayer.getTotalSamples() - seekNumber; i++) {
                samples = flacPlayer.nextSamples();
                var row = Arrays
                        .stream(samples)
                        .mapToObj(String::valueOf)
                        .collect(Collectors.joining(","));
                out.println(row);
            }
        } catch (IOException e) {
            throw new RuntimeException("error", e);
        }
    }
}
