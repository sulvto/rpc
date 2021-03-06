package me.qinchao.protocol.impl;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelFutureListener;
import me.qinchao.api.RpcRequest;
import me.qinchao.api.RpcResponse;
import me.qinchao.service.HelloServiceImpl;
import org.junit.Test;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.lang.reflect.Method;

import static org.junit.Assert.*;

/**
 * Created by SULVTO on 16-4-17.
 */
public class NettyExportTest {

    @Test
    public void testStartServer() throws Exception {
        Object serviceObject = new HelloServiceImpl();
        new NettyExport((channelHandlerContext, rpcRequest) -> {
            RpcResponse rpcResponse = new RpcResponse();
            try {
                String methodName = rpcRequest.getMethodName();
                Class<?>[] parameterTypes = rpcRequest.getParameterTypes();
                Object[] arguments = rpcRequest.getArguments();
                Method method = serviceObject.getClass().getMethod(methodName, parameterTypes);
                Object result = method.invoke(serviceObject, arguments);

                rpcResponse.setResult(result);
            } catch (Exception e) {
                rpcResponse.setException(e);
            }
            channelHandlerContext.writeAndFlush(rpcResponse).addListener(ChannelFutureListener.CLOSE);
        }).startServer("127.0.0.1", 9999);
    }
}