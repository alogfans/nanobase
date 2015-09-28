package com.alogfans.nanobase.rpc.client;

import com.alogfans.nanobase.rpc.serialize.RpcRequest;
import com.alogfans.nanobase.rpc.serialize.RpcRequestEncoder;
import com.alogfans.nanobase.rpc.serialize.RpcResponse;
import com.alogfans.nanobase.rpc.serialize.RpcResponseDecoder;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * RpcClient
 * Created by Alogfans on 2015/9/27.
 */
public class RpcClient {
    private String host;
    private int port;
    private Channel channel;
    private final ConcurrentMap<String, Invoker> dispatchReferences = new ConcurrentHashMap<String, Invoker>();
    private EventLoopGroup workerGroup;

    public RpcClient(String host, int port) {
        this.host = host;
        this.port = port;
        this.workerGroup = new NioEventLoopGroup();
    }

    public void sendRpcRequest(RpcRequest rpcRequest, Invoker invoker) {
        dispatchReferences.put(rpcRequest.getUid(), invoker);
        channel.writeAndFlush(rpcRequest);
    }

    public Invoker createInvoker() {
        return new Invoker().setRpcClient(this);
    }

    public void run() throws Exception {
        Bootstrap bootstrap = new Bootstrap();
        bootstrap.group(workerGroup)
                .channel(NioSocketChannel.class)
                .option(ChannelOption.SO_KEEPALIVE, true)
                .handler(new ChannelInitializer<SocketChannel>() {
                    @Override
                    protected void initChannel(SocketChannel ch) throws Exception {
                        ch.pipeline().addLast(new RpcRequestEncoder(),
                                new RpcResponseDecoder(),
                                new RpcClientHandler(dispatchReferences));
                    }
                });

        ChannelFuture future = bootstrap.connect(host, port).sync();
        channel = future.channel();
    }

    public void stop() {
        workerGroup.shutdownGracefully();
    }
}
