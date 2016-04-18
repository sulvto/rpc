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
import java.util.concurrent.locks.AbstractQueuedSynchronizer;

/**
 * Created by SULVTO on 16-4-17.
 */
public class NettyRefer {

    private Channel channel;

    private CountDownLatch responseDone;
    private volatile boolean isConnect = false;
    private RPCFuture<RpcResponse> rpcFuture;
    private String host;
    private int port;

    public NettyRefer(String host, int port) {
        this.host = host;
        this.port = port;
        responseDone = new CountDownLatch(1);
    }

    private void doConnect() throws InterruptedException {
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
                                protected void messageReceived(ChannelHandlerContext ctx, RpcResponse msg) throws Exception {
                                    rpcFuture.done(msg);
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
            isConnect = true;
            channel = channelFuture.channel();
            channel.closeFuture().sync();
        } finally {
            bossGroup.shutdownGracefully();
        }
    }

    public void connect() {
        rpcFuture = new RPCFuture();
        new Thread(() -> {
            try {
                doConnect();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }).start();
    }

    public RpcResponse refer(RpcRequest rpcRequest) throws InterruptedException {
        connect();
        while (!isConnect) {
            TimeUnit.MILLISECONDS.sleep(100);
        }
        channel.writeAndFlush(rpcRequest).sync();

        return rpcFuture.get();
    }

    public class RPCFuture<T> implements Future {
        private T result;
        private Sync sync;

        protected RPCFuture() {
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
            sync.tryAcquire(-1);
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
