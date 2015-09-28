package com.alogfans.nanobase.rpc.client;

import com.alogfans.nanobase.rpc.serialize.RpcRequest;
import com.alogfans.nanobase.rpc.serialize.RpcResponse;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.LinkedList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;


/**
 * Invoker
 *
 * Created by Alogfans on 2015/9/28.
 */
public class Invoker implements InvocationHandler {
    private Class<?> interfaceClazz;
    private RpcClient rpcClient;
    private Object instance;

    private BlockingQueue<RpcResponse> blockingQueue;

    public Invoker() {
        blockingQueue = new LinkedBlockingQueue<RpcResponse>();
    }

    public Invoker setInterfaceClazz(Class<?> interfaceClazz) {
        this.interfaceClazz = interfaceClazz;
        return this;
    }

    public Invoker setRpcClient(RpcClient rpcClient) {
        this.rpcClient = rpcClient;
        return this;
    }

    public Class<?> getInterfaceClazz() {
        return interfaceClazz;
    }

    public Object getInstance() {
        if (instance == null) {
            instance = Proxy.newProxyInstance(this.getClass().getClassLoader(),
                    new Class[]{this.interfaceClazz},
                    this);
        }
        return instance;
    }

    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        List<String> parameterTypes = new LinkedList<String>();
        for (Class<?> type : method.getParameterTypes()) {
            parameterTypes.add(type.getName());
        }

        RpcRequest rpcRequest = new RpcRequest(generateUid(),
                interfaceClazz.getName(),
                method.getName(),
                parameterTypes.toArray(new String[parameterTypes.size()]),
                args);

        rpcClient.sendRpcRequest(rpcRequest, this);

        RpcResponse rpcResponse = blockingQueue.poll(1, TimeUnit.SECONDS);

        if (rpcResponse == null)
            throw new RuntimeException("Broken response");

        if (rpcResponse.getException() != null)
            throw rpcResponse.getException();
        return rpcResponse.getResult();
    }

    public void notifyResponse(RpcResponse rpcResponse) {
        blockingQueue.add(rpcResponse);
    }

    private String generateUid() {
        return UUID.randomUUID().toString();
    }
}
