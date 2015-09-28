package com.alogfans.nanobase.rpc.server;

import com.alogfans.nanobase.rpc.serialize.RpcRequest;
import com.alogfans.nanobase.rpc.serialize.RpcResponse;

import java.lang.reflect.Method;
import java.util.LinkedList;
import java.util.List;

/**
 * Provider
 *
 * Created by Alogfans on 2015/9/28.
 */
public class Provider {
    private Class<?> interfaceClazz;
    private Object instance;

    public Provider setInterfaceClazz(Class<?> interfaceClazz) {
        this.interfaceClazz = interfaceClazz;
        return this;
    }

    public Provider setInstance(Object instance) {
        this.instance = instance;
        return this;
    }

    public String getInterfaceClazzName() {
        return interfaceClazz.getName();
    }

    public RpcResponse invoke(RpcRequest rpcRequest) {
        RpcResponse rpcResponse = new RpcResponse();
        rpcResponse.setUid(rpcRequest.getUid());

        try {
            List<Class<?>> parameterTypes = new LinkedList<Class<?>>();

            for (String type: rpcRequest.getParameterTypes()) {
                parameterTypes.add(Class.forName(type));
            }

            Method method = interfaceClazz.getMethod(rpcRequest.getMethodName(),
                    parameterTypes.toArray(new Class<?>[parameterTypes.size()]));
            Object result = method.invoke(instance, rpcRequest.getParameters());
            rpcResponse.setResult(result);
        } catch (Exception e) {
            rpcResponse.setException(e);
        }
        return rpcResponse;
    }
}
