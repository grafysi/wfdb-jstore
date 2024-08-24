package io.graphys.wfdbjstore.dailygreeting;

import io.graphys.wfdbjstore.dailygreeting.domain.Customer;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class RegisterCustomerHandler extends ChannelInboundHandlerAdapter {
    private static final Logger logger = LogManager.getLogger(RegisterCustomerHandler.class);

    private final ExecutorService executorService = Executors.newVirtualThreadPerTaskExecutor();
    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        var customer = (Customer) msg;
        //logger.info("Registered greeting service: {}", customer);

        //Thread.sleep(200);
        executorService.submit(new GreetingAgent(ctx, customer, 100));
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        throw new RuntimeException(cause);
    }
}
