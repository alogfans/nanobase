package com.alogfans.nanobase.rpc;

/**
 * Created by Alogfans on 2015/9/28.
 */
public class EchoImpl implements IEcho {
    public String echo(String in) throws Exception {
        // throw new Exception("Hello world!");
        return in;
    }
}
