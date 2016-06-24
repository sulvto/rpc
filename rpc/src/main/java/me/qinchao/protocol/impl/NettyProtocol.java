package me.qinchao.protocol.impl;

import io.netty.channel.ChannelFutureListener;
import me.qinchao.api.*;
import me.qinchao.protocol.Protocol;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;

/**
 * Created by SULVTO on 16-4-3.
 */
public class NettyProtocol implements Protocol {


    @Override
    public void export(Object serviceObject, String host, int port) {

        new NettyExport((context, rpcRequest) -> {
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
            context.writeAndFlush(rpcResponse).addListener(ChannelFutureListener.CLOSE);
        }).startServer(host, port);

    }

    @Override
    public <T> T refer(Class<T> serviceType, String host, int port) {
        return (T) Proxy.newProxyInstance(serviceType.getClassLoader(), new Class<?>[]{serviceType}, new InvocationHandler() {
                    @Override
                    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
                        RpcRequest rpcRequest = new RpcRequest();
                        rpcRequest.setMethodName(method.getName());
                        rpcRequest.setParameterTypes(method.getParameterTypes());
                        rpcRequest.setArguments(args);
                        return new NettyRefer(host, port)
                                .refer(rpcRequest);
                    }
                }
        );
    }
}
