package me.qinchao.protocol.impl;

import com.google.common.util.concurrent.FutureCallback;
import com.google.common.util.concurrent.FutureFallback;
import com.sun.corba.se.impl.orbutil.concurrent.Sync;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import me.qinchao.api.*;

import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.locks.AbstractQueuedSynchronizer;
import java.util.function.Consumer;
import java.util.function.Function;

/**
 * Created by SULVTO on 16-4-17.
 */
public class NettyRefer {

    private Channel channel;

    private ResultFuture<RpcResponse> resultFuture;
    private String host;
    private int port;

    public NettyRefer(String host, int port) {
        this.host = host;
        this.port = port;
    }

    private void doConnect(Consumer<ChannelHandlerContext> onChannelActive) throws InterruptedException {
        EventLoopGroup bossGroup = new NioEventLoopGroup();
        try {
            Bootstrap bootstrap = new Bootstrap();
            bootstrap.group(bossGroup)
                    .channel(NioSocketChannel.class)
                    .handler(new ChannelInitializer<SocketChannel>() {

                        @Override
                        protected void initChannel(SocketChannel socketChannel) throws Exception {
                            ChannelPipeline pipeline = socketChannel.pipeline();
                            pipeline.addLast(new RpcEncoder());
                            pipeline.addLast(new RpcDecoder());
                            pipeline.addLast(new SimpleChannelInboundHandler<RpcResponse>() {

                                @Override
                                public void channelActive(ChannelHandlerContext ctx) {
                                    onChannelActive.accept(ctx);
                                }

                                @Override
                                protected void messageReceived(ChannelHandlerContext ctx, RpcResponse msg) throws Exception {
                                    resultFuture.done(msg);
                                }

                                @Override
                                public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
                                    ctx.close();
                                }
                            });
                        }
                    });
            bootstrap.option(ChannelOption.TCP_NODELAY, true);

            ChannelFuture channelFuture = bootstrap.connect(host, port).sync();
            channel = channelFuture.channel();

            channel.closeFuture().sync();
        } finally {
            bossGroup.shutdownGracefully();
        }
    }

    private void connect(Consumer<ChannelHandlerContext> onChannelActive) {
        resultFuture = new ResultFuture();
        new Thread(() -> {
            try {
                doConnect(onChannelActive);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }).start();
    }

    public Object refer(RpcRequest rpcRequest) throws Exception {
        connect(context -> {
            context.writeAndFlush(rpcRequest);
        });

        RpcResponse rpcResponse = resultFuture.get();
        if (rpcResponse.getException() != null) {
            throw rpcResponse.getException();
        }
        return rpcResponse.getResult();
    }

    public class ResultFuture<T> implements Future {
        private T result;
        private Sync sync;

        protected ResultFuture() {
            this.sync = new Sync();
        }

        @Override
        @Deprecated
        public boolean cancel(boolean mayInterruptIfRunning) {
            throw new UnsupportedOperationException();
        }

        @Override
        public boolean isCancelled() {
            return false;
        }

        @Override
        public boolean isDone() {
            return sync.isDone();
        }

        public void done(T result) {
            this.result = result;
            sync.release(1);
        }

        @Override
        public T get() {
            sync.acquire(-1);
            return result;
        }

        @Override
        public Object get(long timeout, TimeUnit unit) throws InterruptedException, ExecutionException, TimeoutException {
            throw new UnsupportedOperationException();
        }
    }

    class Sync extends AbstractQueuedSynchronizer {
        private final int done = 1;
        private final int pending = 0;

        protected boolean tryAcquire(int acquires) {
            return getState() == done ? true : false;
        }

        protected boolean tryRelease(int releases) {
            if (getState() == pending) {
                if (compareAndSetState(pending, done)) {
                    return true;
                }
            }
            return false;
        }

        public boolean isDone() {
            getState();
            return getState() == done;
        }
    }
}
