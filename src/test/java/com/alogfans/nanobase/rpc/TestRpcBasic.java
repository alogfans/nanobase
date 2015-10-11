package com.alogfans.nanobase.rpc;

import org.junit.Assert;
import org.junit.Test;

import com.alogfans.nanobase.rpc.client.RpcClient;

/**
 * Created by Alogfans on 2015/9/28.
 */
public class TestRpcBasic {
    @Test
    public void testRpc() {
        new Thread(new Runnable() {
            public void run() {
                new EchoImpl().execute();
            }
        }).start();

        RpcClient rpcClient = new RpcClient("localhost", 8080);
        IEcho invoker = (IEcho) rpcClient.createInvoker()
                        .setInterfaceClazz(IEcho.class)
                        .getInstance();

        try {
            rpcClient.run();
            String[] strings = new String[1000];
            for (int i = 0; i < 1000; ++i)
                strings[i] = invoker.echo(String.format("%d", i));
            for (int i = 0; i < 1000; ++i)
                Assert.assertEquals(Integer.parseInt(strings[i]), i);
            rpcClient.stop();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
