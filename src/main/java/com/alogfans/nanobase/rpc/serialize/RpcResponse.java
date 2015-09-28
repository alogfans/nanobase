package com.alogfans.nanobase.rpc.serialize;

import java.io.Serializable;

/**
 * RpcResponse packet: server -> client.
 * Created by Alogfans on 2015/9/27.
 */
public class RpcResponse implements Serializable {
    private static final long serialVersionUID = 6443147893553933129L;

    private String uid;             // identify the request in response packet.
    private Throwable exception;    // when exception happens, it should be transferred back to user agent.
    private Object result;          // returned value.

    public RpcResponse() {

    }

    public RpcResponse setUid(String uid) {
        this.uid = uid;
        return this;
    }

    public String getUid() {
        return uid;
    }

    public RpcResponse setException(Throwable exception) {
        this.exception = exception;
        return this;
    }

    public Throwable getException() {
        return exception;
    }

    public RpcResponse setResult(Object result) {
        this.result = result;
        return this;
    }

    public Object getResult() {
        return result;
    }

    @Override
    public String toString() {
        return String.format("[uid=%s]\n", uid);
    }
}
