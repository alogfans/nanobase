package com.alogfans.nanobase.rpc;

import com.alogfans.nanobase.rpc.client.RpcClient;

/**
 * Created by Alogfans on 2015/9/28.
 */
public class ClientDrive {
    public static void main(String[] args) {
        RpcClient rpcClient = new RpcClient("localhost", 8080);
        IEcho invoker = (IEcho) rpcClient.createInvoker()
                        .setInterfaceClazz(IEcho.class)
                        .getInstance();

        try {
            rpcClient.run();
            String[] strings = new String[10000];
            for (int i = 0; i < 10000; ++i)
                strings[i] = invoker.echo(String.format("%d", i));
            for (int i = 0; i < 10000; ++i)
                if (Integer.parseInt(strings[i]) != i )
                    System.out.print("fuck you!\n");
            // There should be none f**k you displayed in screen, otherwise the programmer
            // would say about this so loudly.
            rpcClient.stop();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
