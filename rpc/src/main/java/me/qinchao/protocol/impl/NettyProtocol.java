package me.qinchao.protocol.impl;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.DelimiterBasedFrameDecoder;
import io.netty.handler.codec.string.StringDecoder;
import me.qinchao.api.AbstractConfig;
import me.qinchao.api.ProtocolConfig;
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

/**
 * Created by SULVTO on 16-4-3.
 */
public class NettyProtocol implements Protocol {

    class ServerHandler extends ChannelHandlerAdapter {
        @Override
        public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
            ByteBuf buf = (ByteBuf) msg;

            byte[] req = new byte[buf.readableBytes()];
            buf.readBytes(req);
            String body = new String(req, "UTF-8");


            ctx.write(resp);
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

    private void bind(int port) {
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
                        pipeline.addLast(new DelimiterBasedFrameDecoder(1024,Unpooled.copiedBuffer("$_".getBytes())));
                        pipeline.addLast(new StringDecoder());
                        pipeline.addLast(new ServerHandler());
                    }
                });
    }

    private void doExport(Object service, int port) throws IOException {

        EventLoopGroup group = new NioEventLoopGroup();


        ServerSocket server = new ServerSocket(port);
        Runnable runnable = () -> {

            for (; ; ) {
                try {
                    Socket socket = server.accept();
                    new Thread(() -> {
                        try {
                            try {
                                ObjectInputStream objectInputStream = new ObjectInputStream(socket.getInputStream());
                                try {
                                    String methodName = objectInputStream.readUTF();
                                    Class<?>[] parameterTypes = (Class<?>[]) objectInputStream.readObject();
                                    Object[] arguments = (Object[]) objectInputStream.readObject();
                                    ObjectOutputStream objectOutputStream = new ObjectOutputStream(socket.getOutputStream());
                                    try {
                                        Method method = service.getClass().getMethod(methodName, parameterTypes);
                                        Object result = method.invoke(service, arguments);
                                        objectOutputStream.writeObject(result);
                                    } catch (Exception e) {
                                        e.printStackTrace();
                                    } finally {
                                        objectOutputStream.close();
                                    }
                                } catch (Exception e) {
                                    e.printStackTrace();
                                } finally {
                                    objectInputStream.close();
                                }
                            } catch (IOException e) {
                                e.printStackTrace();
                            } finally {
                                socket.close();
                            }
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }).start();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }

        };
        new Thread(runnable).start();
    }

    @Override
    public void export(Object serviceObject, ProtocolConfig protocolConfig) {
        try {
            doExport(serviceObject, protocolConfig.getPort());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public <T> T refer(Class<T> serviceType, AbstractConfig protocolConfig) {
        return (T) Proxy.newProxyInstance(serviceType.getClassLoader(), new Class<?>[]{serviceType}, new InvocationHandler() {
                    @Override
                    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
                        try (Socket socket = new Socket(protocolConfig.getHost(), protocolConfig.getPort())) {
                            try (ObjectOutputStream objectOutputStream = new ObjectOutputStream(socket.getOutputStream())) {
                                objectOutputStream.writeUTF(method.getName());
                                objectOutputStream.writeObject(method.getParameterTypes());
                                objectOutputStream.writeObject(args);
                                try (ObjectInputStream input = new ObjectInputStream(socket.getInputStream())) {
                                    Object result = input.readObject();
                                    if (result instanceof Throwable) {
                                        throw (Throwable) result;
                                    }
                                    return result;
                                }
                            }
                        }
                    }
                }
        );
    }
}
