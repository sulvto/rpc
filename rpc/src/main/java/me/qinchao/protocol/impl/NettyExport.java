package me.qinchao.protocol.impl;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import me.qinchao.api.RpcDecoder;
import me.qinchao.api.RpcEncoder;
import me.qinchao.api.RpcRequest;

import java.util.function.BiConsumer;

/**
 * Created by SULVTO on 16-4-17.
 */
public class NettyExport {
    private BiConsumer<ChannelHandlerContext, RpcRequest> onMessageReceived;

    public NettyExport(BiConsumer<ChannelHandlerContext, RpcRequest> onMessageReceived) {
        this.onMessageReceived = onMessageReceived;
    }

    public void doStartServer(String host, int port) throws Exception {

        EventLoopGroup bossGroup = new NioEventLoopGroup();
        EventLoopGroup workerGroup = new NioEventLoopGroup();
        try {
            ServerBootstrap bootstrap = new ServerBootstrap();
            bootstrap.group(bossGroup, workerGroup)
                    .channel(NioServerSocketChannel.class)
                    .childHandler(new ChannelInitializer<SocketChannel>() {

                        @Override
                        protected void initChannel(SocketChannel socketChannel) throws Exception {
                            ChannelPipeline pipeline = socketChannel.pipeline();
                            pipeline.addLast(new RpcDecoder());
                            pipeline.addLast(new RpcEncoder());
                            pipeline.addLast(new SimpleChannelInboundHandler<RpcRequest>() {
                                @Override
                                protected void messageReceived(ChannelHandlerContext ctx, RpcRequest msg) throws Exception {
                                    onMessageReceived.accept(ctx, msg);
                                }

                                @Override
                                public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
                                    ctx.close();
                                }
                            });
                        }
                    })
                    .option(ChannelOption.SO_BACKLOG, 1024)
                    .childOption(ChannelOption.SO_KEEPALIVE, true);
            ChannelFuture future = bootstrap.bind(port).sync();
            future.channel().closeFuture().sync();
        } finally {
            workerGroup.shutdownGracefully().sync();
            bossGroup.shutdownGracefully().sync();
        }
    }


    public void startServer(String host, int port) {
        new Thread(() -> {
            try {
                doStartServer(host, port);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }).start();
    }

}
