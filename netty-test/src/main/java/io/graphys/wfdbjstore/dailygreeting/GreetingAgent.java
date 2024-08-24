package io.graphys.wfdbjstore.dailygreeting;

import io.graphys.wfdbjstore.dailygreeting.domain.Customer;
import io.graphys.wfdbjstore.dailygreeting.domain.Greeting;
import io.netty.channel.ChannelHandlerContext;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.time.LocalDate;
import java.util.Random;

public class GreetingAgent implements Runnable {
    private static final Logger logger = LogManager.getLogger(GreetingAgent.class);
    private Customer customer;
    private LocalDate registerDate = LocalDate.now();
    private int registerDuration;
    private ChannelHandlerContext context;
    private final Random random = new Random();

    public GreetingAgent(ChannelHandlerContext ctx, Customer customer, int registerDuration) {
        this.context = ctx;
        this.customer = customer;
        this.registerDuration = registerDuration;
    }

    @Override
    public void run() {
        for (int i = 0; i < registerDuration; i++) {
            if (!context.channel().isActive()) {
                break;
            }
            var greeting = new Greeting(
                    registerDate.plusDays(i),
                    customer.getName(), customer.getReferredService());
            //logger.info("Agent send: {}", greeting);
            var channelFuture = context.writeAndFlush(greeting);
            channelFuture.addListener(f -> {
                if (f.isSuccess()) {
                    //logger.info("send greeting success...");
                } else {
                    logger.info("send greeting failed...");
                }
            });
            try {
                Thread.sleep(300 + random.nextInt(300));
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }

        var channelFuture = context.close();
        channelFuture.addListener(f -> {
            if (f.isSuccess()) {
                //logger.info("{}-{} close success...", customer.getName(), customer.getId());
            } else {
                //logger.info("{}-{} close failed...", customer.getName(), customer.getId());
            }
        });
    }

}
