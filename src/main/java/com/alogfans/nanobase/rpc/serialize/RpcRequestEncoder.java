package com.alogfans.nanobase.rpc.serialize;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToByteEncoder;

/**
 * Encoder of RpcRequest: object -> bit stream
 * Created by Alogfans on 2015/9/27.
 */
public class RpcRequestEncoder extends MessageToByteEncoder {
    @Override
    protected void encode(ChannelHandlerContext ctx, Object msg, ByteBuf out) throws Exception {
        byte[] rawRpcRequest = BuiltInSerializer.serialize(msg);
        out.writeInt(rawRpcRequest.length);
        out.writeBytes(rawRpcRequest);
    }
}
