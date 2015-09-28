package com.alogfans.nanobase.rpc.serialize;

import java.io.Serializable;

/**
 * RpcRequest packet: client -> server.
 * Created by Alogfans on 2015/9/27.
 */
public class RpcRequest implements Serializable {
    private static final long serialVersionUID = 6443147893553933129L;

    private String uid;             // identify the request in response packet.
    private String className;       // the Java class expected to be invoked.
    private String methodName;      // the method name (allows overloading).
    private String[] parameterTypes;
    private Object[] parameters;    // transfers variable parameters, also supports overload.

    public RpcRequest() {

    }

    public RpcRequest(String uid,
                      String className,
                      String methodName,
                      String[] parameterTypes,
                      Object[] parameters) {
        this.uid = uid;
        this.className = className;
        this.methodName = methodName;
        this.parameterTypes = parameterTypes;
        this.parameters = parameters;
    }

    public String getUid() {
        return uid;
    }

    public String getClassName() {
        return className;
    }

    public String getMethodName() {
        return methodName;
    }

    public String[] getParameterTypes() {
        return parameterTypes;
    }

    public Object[] getParameters() {
        return parameters;
    }

    @Override
    public String toString() {
        return String.format("[uid=%s, className=%s, methodName=%s]\n", uid, className, methodName);
    }
}
