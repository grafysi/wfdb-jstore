package io.graphys.wfdbjstore.httpclient;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.codec.http.*;
import io.netty.util.CharsetUtil;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.PrintWriter;
import java.util.concurrent.atomic.AtomicInteger;

public class HttpWave4jClientHandler extends SimpleChannelInboundHandler<HttpObject> {
    private static final Logger logger = LogManager.getLogger(HttpWave4jClientHandler.class);
    private AtomicInteger counter = new AtomicInteger(1);

    private final PrintWriter outWritter;

    public HttpWave4jClientHandler(String outPath) {
        try {
            this.outWritter = new PrintWriter(new FileOutputStream(outPath));
        } catch (FileNotFoundException e) {
            logger.error("error", e);
            throw new RuntimeException();
        }
    }

    @Override
    public void channelRead0(ChannelHandlerContext ctx, HttpObject msg) {
        if (msg instanceof HttpResponse response) {
            /*System.err.println("STATUS: " + response.status());
            System.err.println("VERSION: " + response.protocolVersion());
            System.err.println();*/

//            outWritter.println("STATUS: " + response.status());
//            outWritter.println("VERSION: " + response.protocolVersion());
//            outWritter.println();

            if (!response.headers().isEmpty()) {
                for (CharSequence name: response.headers().names()) {
                    for (CharSequence value: response.headers().getAll(name)) {
                        //outWritter.println("HEADER: " + name + " = " + value);
                    }
                }
                outWritter.println();
            }

            if (HttpUtil.isTransferEncodingChunked(response)) {
                //outWritter.println("CHUNKED CONTENT {");
            } else {
                //outWritter.println("CONTENT {");
            }
        }
        if (msg instanceof HttpContent content) {
            //logger.info("Content read count: {}", counter.getAndIncrement());
            //outWritter.print(content.content().toString(CharsetUtil.UTF_8));
            //outWritter.flush();

            if (content instanceof LastHttpContent) {
                //outWritter.println("} END OF CONTENT");
                ctx.close();
            }
        }
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) {
        outWritter.close();
        logger.info("File writer closed.");
    }


}
























