package io.graphys.wfdbstore.client;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.junit.jupiter.api.Test;

import java.io.*;
import java.net.*;
import java.nio.ByteBuffer;
import java.nio.channels.*;
import java.util.concurrent.*;


public class DownloadFileTest {
    private static final Logger logger = LogManager.getLogger(DownloadFileTest.class);

    @Test
    public void testDownloadFile() {
        try {
            // Create a URL object
            URL url = new URL("https://physionet.org/files/mimic4wdb/0.1.0/RECORDS");

            // Create an asynchronous channel group
            AsynchronousChannelGroup group = AsynchronousChannelGroup.withFixedThreadPool(1, Executors.defaultThreadFactory());

            // Open an asynchronous socket channel
            AsynchronousSocketChannel channel = AsynchronousSocketChannel.open(group);

            // Connect to the server
            Future<Void> connectFuture = channel.connect(new InetSocketAddress("https://physionet.org/files/mimic4wdb/0.1.0/RECORDS", 80));

            // Wait for the connection to be established
            connectFuture.get();

            logger.info("Connected...");

            // ... now you can perform asynchronous read/write operations ...
            var buffer = ByteBuffer.allocate(20_000);

            logger.info("Start read...");
            var readFuture = channel.read(buffer);

            var bytesRead = readFuture.get();

            logger.info("Bytes read: {}", bytesRead);


        } catch (Exception e) {
            logger.error("error", e);
        } finally {
            // Shutdown the group

        }
    }
}
