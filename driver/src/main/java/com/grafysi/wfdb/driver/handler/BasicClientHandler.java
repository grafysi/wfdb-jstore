package com.grafysi.wfdb.driver.handler;

import com.grafysi.wfdb.driver.domain.Report;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import lombok.Setter;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import com.grafysi.wfdb.driver.domain.Command;

import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;

public class BasicClientHandler extends ChannelInboundHandlerAdapter {

    private static final Logger logger = LogManager.getLogger(BasicClientHandler.class);

    @Setter
    private ChannelHandlerContext context;

    private final ReentrantLock lock = new ReentrantLock();

    private final Condition reportReceived = lock.newCondition();

    private Report report = null;

    private boolean reportAvail = false;

    public BasicClientHandler(ChannelHandlerContext ctx) {
        this.context = ctx;
    }

    public BasicClientHandler() {

    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) {
        lock.lock();
        try {
            if (msg instanceof Report rep) {
                report = rep;
                reportAvail = true;
                reportReceived.signalAll();
            } else {
                throw new RuntimeException("Unexpected message read " + msg.getClass().getName());
            }
        } finally {
            lock.unlock();
        }

    }

    public Report awaitSendCommand(Command command) throws InterruptedException {
        var wf = context.writeAndFlush(command).sync();
        if (wf.isSuccess()) {
            lock.lock();
            try {
                while (!reportAvail) {
                    reportReceived.await();
                }
                var result = report;
                report = null;
                reportAvail = false;
                return result;
            } finally {
                lock.unlock();
            }
        }
        throw new RuntimeException("Command send failed");
    }

    public ChannelFuture closeChannel() {
        return context.close();
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        logger.error("error", cause);
    }
}




















