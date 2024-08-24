package io.graphys.wfdbjstore.recordstore.io;

import io.graphys.wfdbjstore.recordstore.exception.ReadingInterruptedException;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.*;

/**
 * This class wraps an {@link InputStream}. At surface level, this class acts exactly the same as the input stream.
 * However, under the hood, read methods in ordinary input stream have been re-implemented to change one behavior of them is that
 * the reads return -1 only if both EOF reached and {@link CacheInputCoordinatorImpl.Reading#waitNextBytes}
 * returned false.
 */
public class CoordinatedInputStream extends InputStream {
    private static final Logger logger = LogManager.getLogger(CoordinatedInputStream.class);
    private final InputStream inStream;
    private final CacheInputCoordinatorImpl.Reading readingHandler;


    public CoordinatedInputStream(InputStream inStream, CacheInputCoordinatorImpl.Reading readingHandler) {
        this.inStream = inStream;
        this.readingHandler = readingHandler;
    }

    // re-impl: wait next bytes avail instead of return -1 when eof reached
    @Override
    public int read() throws IOException {
        var readByte = inStream.read();
        var shouldRetry = readingHandler != null && readByte == - 1;

        while (shouldRetry) {
            try {
                var availNext = readingHandler.waitNextBytes();
                if (availNext) {
                    shouldRetry = (readByte = inStream.read()) == -1;
                }
                else {
                    shouldRetry = false;
                }
            }
            catch (InterruptedException e) {
                throw new ReadingInterruptedException(e.getMessage());
            }
        }
        return readByte;
    }

    // re-impl: wait next bytes avail instead of return -1 when eof reached
    @Override
    public int read(byte[] b, int off, int len) throws IOException {
        var bytesRead = inStream.read(b, off, len);
        var shouldRetry = readingHandler != null && bytesRead == -1;

        //logger.info("go here #1");

        while (shouldRetry) {
            try {
                var availNext = readingHandler.waitNextBytes();
                //logger.info("go here #2");
                if (availNext) {
                    //logger.info("go here #3");
                    shouldRetry = (bytesRead = inStream.read(b, off, len)) == -1;
                }
                else {
                    shouldRetry = false;
                }
            }
            catch (InterruptedException e) {
                throw new ReadingInterruptedException(e.getMessage());
            }
        }

        return bytesRead;
    }

    // re-impl: wait next bytes avail instead of return -1 when eof reached
    @Override
    public int read(byte[] b) throws IOException {
        return read(b, 0, b.length  );
    }

    // close wrapped stream and acknowledge reading complete
    @Override
    public void close() throws IOException {
        inStream.close();
        readingHandler.readerCompleted();
    }
}
































