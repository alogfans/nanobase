package com.alogfans.nanobase.rpc;

import com.alogfans.nanobase.rpc.server.Provider;
import com.alogfans.nanobase.rpc.server.RpcServer;

/**
 * Created by Alogfans on 2015/9/28.
 */
public class ServerDrive {
    public static void main(String[] args) {
        new EchoImpl().execute();
    }
}
