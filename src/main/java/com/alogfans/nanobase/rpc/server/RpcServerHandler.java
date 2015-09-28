package com.alogfans.nanobase.rpc.server;

import com.alogfans.nanobase.rpc.serialize.RpcRequest;
import com.alogfans.nanobase.rpc.serialize.RpcResponse;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;

import java.util.Map;

/**
 * Rpc Server Handler
 *
 * Created by Alogfans on 2015/9/27.
 */
public class RpcServerHandler extends ChannelInboundHandlerAdapter {
    private final Map<String, Provider> dispatchReferences;

    public RpcServerHandler(Map<String, Provider> dispatchReferences) {
        this.dispatchReferences = dispatchReferences;
    }

    /**
     * Fetch a request and dispatch to corresponding provider.
     * @param ctx ChannelHandlerContext object
     * @param msg RpcRequest object
     * @throws Exception
     */
    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        RpcRequest rpcRequest = (RpcRequest) msg;
        if (dispatchReferences.containsKey(rpcRequest.getClassName())) {
            Provider provider = dispatchReferences.get(rpcRequest.getClassName());
            RpcResponse rpcResponse = provider.invoke(rpcRequest);
            ctx.writeAndFlush(rpcResponse);
        }
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        ctx.close().sync();
        cause.printStackTrace();
    }
}
