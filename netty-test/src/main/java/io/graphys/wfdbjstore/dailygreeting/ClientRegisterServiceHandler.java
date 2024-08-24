package io.graphys.wfdbjstore.dailygreeting;

import io.graphys.wfdbjstore.dailygreeting.domain.Customer;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.util.ReferenceCountUtil;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.PrintWriter;

public class ClientRegisterServiceHandler extends ChannelInboundHandlerAdapter {
    private static final Logger logger = LogManager.getLogger(ClientRegisterServiceHandler.class);
    private static final String BASE_PATH = "data/output";
    private Customer customer;

    public ClientRegisterServiceHandler(Customer customer) {
        this.customer = customer;
    }

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        //logger.info("Send message: {}", customer);
        var channelFuture = ctx.writeAndFlush(customer);
        channelFuture.addListener(f -> {
            if (f.isSuccess()) {
                //logger.info("Send success...");
            } else {
                logger.info("Send failed...");
            }

            if (f.cause() != null) {
                logger.info(f.cause());
            }
        });
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) {
        try (var out = new PrintWriter(new FileOutputStream(BASE_PATH + "/" + customer.getName() + "-" + customer.getId(), true))) {
            //logger.info("Received: {}", msg);
            out.println(msg);
            //ReferenceCountUtil.release(msg);
        } catch (FileNotFoundException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) {
        logger.info("{}-{} closed...", customer.getName(), customer.getId());
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        ctx.close();
        logger.error(cause);
        throw new RuntimeException(cause);
    }

}

















