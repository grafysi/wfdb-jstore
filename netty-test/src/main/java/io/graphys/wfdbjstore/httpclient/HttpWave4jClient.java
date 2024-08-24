package io.graphys.wfdbjstore.httpclient;

/*
 * Copyright 2012 The Netty Project
 *
 * The Netty Project licenses this file to you under the Apache License,
 * version 2.0 (the "License"); you may not use this file except in compliance
 * with the License. You may obtain a copy of the License at:
 *
 *   https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations
 * under the License.
 */

import io.netty.bootstrap.Bootstrap;
import io.netty.buffer.Unpooled;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.http.DefaultFullHttpRequest;
import io.netty.handler.codec.http.HttpHeaderNames;
import io.netty.handler.codec.http.HttpHeaderValues;
import io.netty.handler.codec.http.HttpMethod;
import io.netty.handler.codec.http.HttpRequest;
import io.netty.handler.codec.http.HttpVersion;
import io.netty.handler.codec.http.cookie.ClientCookieEncoder;
import io.netty.handler.codec.http.cookie.DefaultCookie;
import io.netty.handler.ssl.SslContext;
import io.netty.handler.ssl.SslContextBuilder;
import io.netty.handler.ssl.util.InsecureTrustManagerFactory;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.net.URI;
import java.time.Duration;
import java.time.Instant;
import java.util.LinkedList;
import java.util.concurrent.Executors;
import java.util.stream.IntStream;

public final class HttpWave4jClient {
    private static final Logger logger = LogManager.getLogger(HttpWave4jClient.class);

    static final String URL = System.getProperty("url", "https://physionet.org/files/mimic4wdb/0.1.0/waves/p101/p10100546/83268087/83268087.hea");

    static final String REMOTE_ROOT = "https://physionet.org/files/mimic4wdb/0.1.0/waves/p101/p10100546/83268087/";
    static final String LOCAL_ROOT = "/home/nhtri/wfdb-jstore/netty-test/data/output/";

    public static void main(String[] args) throws Exception {
        var baseName = 83268087;
        var postFix = ".hea";
        var numFiles = 196;
        var nameList = new LinkedList<String>();
        nameList.add(baseName + postFix);
        IntStream.range(0, numFiles).forEach(i -> nameList.add(String.format("%s_%04d%s", baseName, i, postFix)));

        var start = Instant.now();
        try (var workerGroup = new NioEventLoopGroup(4)) {
            var futures = nameList.stream()
                    .map(s -> {
                        try {
                            return downloadFile(s, workerGroup);
                        } catch (Exception e) {
                            throw new RuntimeException("Error on: " + s, e);
                        }})
                    .toList();

            for (var future: futures) {
                future.sync();
            }
        }

        logger.error("Downloaded {} files in {} ms", numFiles, Duration.between(start, Instant.now()).toMillis());
    }

    public static ChannelFuture downloadFile(String fileName, NioEventLoopGroup group) throws Exception {
        URI uri = new URI(REMOTE_ROOT + fileName);
        String scheme = uri.getScheme() == null? "http" : uri.getScheme();
        String host = uri.getHost() == null? "127.0.0.1" : uri.getHost();
        int port = uri.getPort();
        if (port == -1) {
            if ("http".equalsIgnoreCase(scheme)) {
                port = 80;
            } else if ("https".equalsIgnoreCase(scheme)) {
                port = 443;
            }
        }

        if (!"http".equalsIgnoreCase(scheme) && !"https".equalsIgnoreCase(scheme)) {
            //System.err.println("Only HTTP(S) is supported.");
            throw new IllegalArgumentException("Only HTTP(S) is supported.");
        }

        // Configure SSL context if necessary.
        final boolean ssl = "https".equalsIgnoreCase(scheme);
        final SslContext sslCtx;
        if (ssl) {
            sslCtx = SslContextBuilder.forClient()
                    .trustManager(InsecureTrustManagerFactory.INSTANCE).build();
        } else {
            sslCtx = null;
        }

        // Configure the client.
        try {
            Bootstrap b = new Bootstrap();
            b.group(group)
                    .channel(NioSocketChannel.class)
                    .handler(new HttpWave4jClientInitializer(sslCtx, LOCAL_ROOT + fileName));

            // start
            var start = Instant.now();

            // Make the connection attempt.
            Channel ch = b.connect(host, port).sync().channel();

            // Prepare the HTTP request.
            HttpRequest request = new DefaultFullHttpRequest(
                    HttpVersion.HTTP_1_1, HttpMethod.GET, uri.getRawPath(), Unpooled.EMPTY_BUFFER);
            request.headers().set(HttpHeaderNames.HOST, host);
            request.headers().set(HttpHeaderNames.CONNECTION, HttpHeaderValues.CLOSE);
            request.headers().set(HttpHeaderNames.ACCEPT_ENCODING, HttpHeaderValues.GZIP);

            // Set some example cookies.
            request.headers().set(
                    HttpHeaderNames.COOKIE,
                    ClientCookieEncoder.STRICT.encode(
                            new DefaultCookie("my-cookie", "foo"),
                            new DefaultCookie("another-cookie", "bar")));

            // Send the HTTP request.
            ch.writeAndFlush(request);

            // Wait for the server to close the connection.
            //ch.closeFuture().sync();
            return ch.closeFuture();

            //logger.info("Total running time: {} ms", Duration.between(start, Instant.now()).toMillis());

        } finally {
            // Shut down executor threads to exit.
            //group.shutdownGracefully();
        }
    }

}








































