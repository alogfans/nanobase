package com.alogfans.nanobase.rpc.client;

import com.alogfans.nanobase.rpc.serialize.RpcResponse;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;

import java.util.Map;
import java.util.concurrent.ConcurrentMap;

/**
 * Created by Alogfans on 2015/9/27.
 */
public class RpcClientHandler extends ChannelInboundHandlerAdapter {
    private final ConcurrentMap<String, Invoker> dispatchReferences;

    public RpcClientHandler(ConcurrentMap<String, Invoker> dispatchReferences) {
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
        RpcResponse rpcResponse = (RpcResponse) msg;
        if (dispatchReferences.containsKey(rpcResponse.getUid())) {
            dispatchReferences.get(rpcResponse.getUid()).notifyResponse(rpcResponse);
            dispatchReferences.remove(rpcResponse.getUid());
        }
    }
}
