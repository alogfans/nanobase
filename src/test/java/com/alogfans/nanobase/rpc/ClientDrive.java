package com.alogfans.nanobase.rpc;

import com.alogfans.nanobase.rpc.client.RpcClient;

/**
 * Created by Alogfans on 2015/9/28.
 */
public class ClientDrive {
    public static void main(String[] args) {
        RpcClient rpcClient = new RpcClient("localhost", 8080);
        IEcho invoker = (IEcho)
                rpcClient.createInvoker().setInterfaceClazz(IEcho.class).getInstance();

        try {
            rpcClient.run();
            for (int i = 0; i < 30; ++i)
                System.out.println(invoker.echo(String.format("Message %d.", i)));
            rpcClient.stop();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
