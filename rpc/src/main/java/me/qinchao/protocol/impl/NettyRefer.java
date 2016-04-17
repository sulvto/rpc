package me.qinchao.protocol.impl;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import me.qinchao.api.*;

/**
 * Created by SULVTO on 16-4-17.
 */
public class NettyRefer {

    private RpcResponse response;
    private final Object obj = new Object();

    private String host;
    private int port;

    public NettyRefer(String host, int port) {
        this.host = host;
        this.port = port;
    }

    public RpcResponse refer(RpcRequest rpcRequest) throws InterruptedException {

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
                                    System.out.println("NettyRefer messageReceived");
                                    response = msg;
                                }

                                @Override
                                public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
                                    ctx.close();
                                }
                            });
                        }
                    }).option(ChannelOption.SO_KEEPALIVE, true);
            bootstrap.option(ChannelOption.TCP_NODELAY, true);

            ChannelFuture channelFuture = bootstrap.connect(host, port).sync();

            Channel channel = channelFuture.channel();
            channel.writeAndFlush(rpcRequest).sync();
            channel.closeFuture().sync();
        } finally {
            bossGroup.shutdownGracefully().sync();
        }

        return response;
    }

}
