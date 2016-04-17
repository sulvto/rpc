package me.qinchao.protocol.impl;

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
    public void export(Object serviceObject, AbstractConfig protocolConfig) {
        new Thread(() -> {
            try {
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
                        rpcResponse.setError(e);
                    }
                    try {
                        context.writeAndFlush(rpcResponse).sync();
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                })
                        .startServer(protocolConfig.getHost(), protocolConfig.getPort());
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }).start();
    }

    @Override
    public <T> T refer(Class<T> serviceType, AbstractConfig protocolConfig) {
        return (T) Proxy.newProxyInstance(serviceType.getClassLoader(), new Class<?>[]{serviceType}, new InvocationHandler() {
                    @Override
                    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
                        RpcRequest rpcRequest = new RpcRequest();
                        rpcRequest.setMethodName(method.getName());
                        rpcRequest.setParameterTypes(method.getParameterTypes());
                        rpcRequest.setArguments(args);
                        return new NettyRefer(protocolConfig.getHost(), protocolConfig.getPort())
                                .refer(rpcRequest);
                    }
                }
        );
    }
}
