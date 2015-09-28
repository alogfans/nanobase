package com.alogfans.nanobase.rpc.server;

import com.alogfans.nanobase.rpc.serialize.RpcRequest;
import com.alogfans.nanobase.rpc.serialize.RpcRequestDecoder;
import com.alogfans.nanobase.rpc.serialize.RpcResponse;
import com.alogfans.nanobase.rpc.serialize.RpcResponseEncoder;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;

import java.util.HashMap;
import java.util.Map;

/**
 * RpcServer
 * Created by Alogfans on 2015/9/27.
 */
public class RpcServer {
    private int port;
    private Channel channel;
    private Map<String, Provider> dispatchReferences;
    private EventLoopGroup bossGroup;
    private EventLoopGroup workerGroup;

    public RpcServer(int port) {
        this.port = port;
        this.dispatchReferences = new HashMap<String, Provider>();
        this.bossGroup = new NioEventLoopGroup();
        this.workerGroup = new NioEventLoopGroup();
    }

    public RpcServer addProvider(Provider provider) {
        dispatchReferences.put(provider.getInterfaceClazzName(), provider);
        return this;
    }

    public void run() throws Exception {
        ServerBootstrap bootstrap = new ServerBootstrap();
        bootstrap.group(bossGroup, workerGroup)
                .channel(NioServerSocketChannel.class)
                .childHandler(new ChannelInitializer<SocketChannel>() {
                    @Override
                    protected void initChannel(SocketChannel ch) throws Exception {
                        ch.pipeline().addLast(new RpcRequestDecoder(),
                                new RpcResponseEncoder(),
                                new RpcServerHandler(dispatchReferences));
                    }
                })
                .option(ChannelOption.SO_BACKLOG, 128)
                .childOption(ChannelOption.SO_KEEPALIVE, true);

        ChannelFuture future = bootstrap.bind(port).sync();
        channel = future.channel();
        channel.closeFuture().sync();
    }

    public void stop() {
        workerGroup.shutdownGracefully();
        bossGroup.shutdownGracefully();
    }

}
