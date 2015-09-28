package com.alogfans.nanobase.rpc.serialize;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.ByteToMessageDecoder;

import java.util.List;

/**
 * Decoder of RpcRequest: bit stream -> object
 * Created by Alogfans on 2015/9/27.
 */
public class RpcRequestDecoder extends ByteToMessageDecoder {
    final int PACKET_HEADER_LENGTH = 4;

    @Override
    protected void decode(ChannelHandlerContext ctx, ByteBuf in, List<Object> out) throws Exception {
        if (in.readableBytes() < PACKET_HEADER_LENGTH)
            return;

        // Set current reader index (nothing read now)
        in.markReaderIndex();
        int packetLength = in.readInt();
        if (packetLength < 0) {
            ctx.close();
            return;
        }

        if (in.readableBytes() < packetLength) {
            // The packet is incomplete, so we will reset the reader index, as if nothing were read.
            // Wait for next loop ...
            in.resetReaderIndex();
            return;
        }

        // Convert the remaining content for deserialization.
        byte[] rawRpcRequest = new byte[packetLength];
        in.readBytes(rawRpcRequest);

        RpcRequest rpcRequest = (RpcRequest) BuiltInSerializer.deserialize(rawRpcRequest);
        out.add(rpcRequest);
    }
}
