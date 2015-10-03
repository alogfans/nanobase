package com.alogfans.nanobase.rpc;

import com.alogfans.nanobase.rpc.server.Provider;
import com.alogfans.nanobase.rpc.server.RpcServer;

/**
 * Created by Alogfans on 2015/9/28.
 */
public class ServerDrive {
    public static void main(String[] args) {
        Provider provider = new Provider()
                .setInterfaceClazz(IEcho.class)
                .setInstance(new EchoImpl());
        RpcServer rpcServer = new RpcServer(8080);
        rpcServer.addProvider(provider);

        try {
            rpcServer.run();
            Thread.sleep(30000);
            System.out.print("Rpc is working in background.");
            rpcServer.join();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            rpcServer.stop();
        }
    }
}
