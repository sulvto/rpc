package me.qinchao.protocol.impl;

import io.netty.bootstrap.Bootstrap;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.DelimiterBasedFrameDecoder;
import io.netty.handler.codec.string.StringDecoder;
import me.qinchao.api.*;
import me.qinchao.protocol.Protocol;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by SULVTO on 16-4-3.
 */
public class NettyProtocol implements Protocol {

    private Object service;
    private RpcResponse response;
    private final Object obj = new Object();


    class RequestHandler extends SimpleChannelInboundHandler<RpcRequest> {

        @Override
        protected void messageReceived(ChannelHandlerContext ctx, RpcRequest rpcRequest) throws Exception {
            RpcResponse rpcResponse = new RpcResponse();
            try {
                String methodName = rpcRequest.getMethodName();
                Class<?>[] parameterTypes = rpcRequest.getParameterTypes();
                Object[] arguments = rpcRequest.getArguments();
                Method method = service.getClass().getMethod(methodName, parameterTypes);
                Object result = method.invoke(service, arguments);

                rpcResponse.setResult(result);
            } catch (Exception e) {
                rpcResponse.setError(e);
            }
            ctx.writeAndFlush(rpcResponse).sync();
        }


        @Override
        public void channelReadComplete(ChannelHandlerContext ctx) throws Exception {
            ctx.flush();
        }

        @Override
        public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
            ctx.close();
        }
    }

    private void startServer(int port) {
        EventLoopGroup bossGroup = new NioEventLoopGroup();
        EventLoopGroup workerGroup = new NioEventLoopGroup();

        ServerBootstrap bootstrap = new ServerBootstrap();
        bootstrap.group(bossGroup, workerGroup)
                .channel(NioServerSocketChannel.class)
                .childOption(ChannelOption.SO_BACKLOG, 1024)
                .childHandler(new ChannelInitializer<SocketChannel>() {

                    @Override
                    protected void initChannel(SocketChannel socketChannel) throws Exception {
                        ChannelPipeline pipeline = socketChannel.pipeline();
                        pipeline.addLast(new RpcCodec());
                        pipeline.addLast(new RequestHandler());
                    }
                });
    }


    @Override
    public void export(Object serviceObject, ProtocolConfig protocolConfig) {
        this.service = service;
        startServer(protocolConfig.getPort());
    }

    @Override
    public <T> T refer(Class<T> serviceType, AbstractConfig protocolConfig) {

        return (T) Proxy.newProxyInstance(serviceType.getClassLoader(), new Class<?>[]{serviceType}, new InvocationHandler() {
                    @Override
                    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
                        EventLoopGroup bossGroup = new NioEventLoopGroup();

                        Bootstrap bootstrap = new Bootstrap();
                        bootstrap.group(bossGroup)
                                .channel(NioServerSocketChannel.class)
                                .handler(new ChannelInitializer<SocketChannel>() {

                                    @Override
                                    protected void initChannel(SocketChannel socketChannel) throws Exception {
                                        ChannelPipeline pipeline = socketChannel.pipeline();
                                        pipeline.addLast(new RpcCodec());
                                        pipeline.addLast(new ResponseHandler());
                                    }
                                });
                        ChannelFuture future = bootstrap.connect(protocolConfig.getHost(), protocolConfig.getPort()).sync();
                        RpcRequest rpcRequest = new RpcRequest();
                        rpcRequest.setClassName(method.getDeclaringClass().getName());
                        rpcRequest.setMethodName(method.getName());
                        rpcRequest.setParameterTypes(method.getParameterTypes());
                        rpcRequest.setArguments(args);

                        future.channel().writeAndFlush(rpcRequest).sync();
                        future.channel().closeFuture().sync();

                        synchronized (obj) {
                            obj.wait();
                        }
                        return response;
                    }
                }
        );
    }

    class ResponseHandler extends SimpleChannelInboundHandler<RpcResponse> {

        @Override
        protected void messageReceived(ChannelHandlerContext ctx, RpcResponse rpcResponse) throws Exception {
            response = rpcResponse;
            synchronized (obj) {
                obj.notifyAll(); // 收到响应，唤醒线程
            }
        }


        @Override
        public void channelReadComplete(ChannelHandlerContext ctx) throws Exception {
            ctx.flush();
        }

        @Override
        public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
            ctx.close();
        }
    }
}
