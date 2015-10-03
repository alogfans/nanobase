package com.alogfans.nanobase.rpc;

import com.alogfans.nanobase.rpc.server.Provider;
import com.alogfans.nanobase.rpc.server.RpcServer;

/**
 * Created by Alogfans on 2015/9/28.
 */
public class EchoImpl implements IEcho {
    public void execute() {
        Provider provider = new Provider()
                .setInterfaceClazz(IEcho.class)
                .setInstance(this);
        RpcServer rpcServer = new RpcServer(8080);
        rpcServer.addProvider(provider);

        try {
            rpcServer.run();
            System.out.print("Rpc is working in background.");
            rpcServer.join();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            rpcServer.stop();
        }
    }

    public String echo(String in) throws Exception {
        return in;
    }
}
