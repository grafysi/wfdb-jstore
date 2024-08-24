package io.graphys.wfdbjstore.httpclient;

import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.socket.SocketChannel;
import io.netty.handler.codec.http.HttpClientCodec;
import io.netty.handler.ssl.SslContext;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class HttpWave4jClientInitializer extends ChannelInitializer<SocketChannel>{
    private static final Logger logger = LogManager.getLogger(HttpWave4jClientInitializer.class);
    private final SslContext sslCtx;
    private final String outPath;

    public HttpWave4jClientInitializer(SslContext sslCtx, String outPath) {
        this.sslCtx = sslCtx;
        this.outPath = outPath;
    }

    @Override
    public void initChannel(SocketChannel ch) {
        try {
            ChannelPipeline p = ch.pipeline();

            // Enable HTTPS if necessary.
            if (sslCtx != null) {
                p.addLast(sslCtx.newHandler(ch.alloc()));
            }

            p.addLast(new HttpClientCodec());

            // Remove the following line if you don't want automatic content decompression.
            //p.addLast(new HttpContentDecompressor());

            // Uncomment the following line if you don't want to handle HttpContents.
            //p.addLast(new HttpObjectAggregator(1048576));

            p.addLast(new HttpWave4jClientHandler(outPath));
        } catch (Exception e) {
            logger.error("init error", e);
        }
    }
}
