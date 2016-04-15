package me.qinchao.api;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufInputStream;
import io.netty.buffer.ByteBufOutputStream;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.ByteToMessageCodec;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.List;

/**
 * Created by SULVTO on 16-4-15.
 */
public class RpcCodec extends ByteToMessageCodec<RpcRequest> {
    @Override
    protected void encode(ChannelHandlerContext ctx, RpcRequest msg, ByteBuf out) throws Exception {
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        ObjectOutputStream objectOutputStream = new ObjectOutputStream(byteArrayOutputStream);
        objectOutputStream.writeObject(msg);
        objectOutputStream.flush();
        objectOutputStream.close();

        byte[] data = byteArrayOutputStream.toByteArray();
        objectOutputStream.close();
        out.writeInt(data.length);
        out.writeBytes(data);
    }

    @Override
    protected void decode(ChannelHandlerContext ctx, ByteBuf in, List<Object> out) throws Exception {
        int length = in.readInt();
        byte[] bytes = new byte[length];
        in.writeBytes(bytes);

        ByteArrayInputStream byteArrayOutputStream = new ByteArrayInputStream(bytes);

        ObjectInputStream objectInputStream = new ObjectInputStream(byteArrayOutputStream);
        Object o = objectInputStream.readObject();
        out.add(o);
        objectInputStream.close();
        byteArrayOutputStream.close();
    }
}
