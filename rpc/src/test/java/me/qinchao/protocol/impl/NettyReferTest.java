package me.qinchao.protocol.impl;

import me.qinchao.api.RpcRequest;
import me.qinchao.api.RpcResponse;
import me.qinchao.service.HelloServiceImpl;
import org.junit.Assert;
import org.junit.Test;

import java.lang.reflect.Method;

/**
 * Created by SULVTO on 16-4-17.
 */
public class NettyReferTest {

    @Test
    public void testRefer() throws Exception {

        RpcRequest rpcRequest = new RpcRequest();
        Method[] methods = HelloServiceImpl.class.getMethods();

        rpcRequest.setParameterTypes(methods[0].getParameterTypes());
        rpcRequest.setMethodName("hello");
        rpcRequest.setArguments(new String[]{"haha"});
        Object result = new NettyRefer("127.0.0.1", 9999).refer(rpcRequest);
        Assert.assertNotNull(result);
    }
}