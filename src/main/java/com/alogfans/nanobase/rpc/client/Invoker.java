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
 * Invoker: could be multiple in single client. But no recommended for using same
 * rpc interface class, otherwise may occur some bugs.
 *
 * Created by Alogfans on 2015/9/28.
 */
public class Invoker implements InvocationHandler {
    // Interface class that expected to apply.
    private Class<?> interfaceClazz;
    // Will call sendRpcRequest function (owns the Channel object for I/O)
    private RpcClient rpcClient;
    // The object with dynamic interface.
    private Object instance;
    // blocking timeout by second.
    private final int TIMEOUT = 2;

    // It's possible that several rpc responses are loaded, so a queue is required.
    // BlockingQueue is a concurrent utility that allowed to get one object by
    // waiting, even if it's empty in the queue.
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

        RpcResponse rpcResponse;
        int sizeQueue = 0;
        rpcResponse = blockingQueue.poll(TIMEOUT, TimeUnit.SECONDS);

        // If the current response is not mine, let it return to queue and try next one.
        while (rpcResponse == null || rpcResponse.getUid().compareTo(rpcRequest.getUid()) != 0) {
            // loop back to the first entry
            if (rpcResponse == null || sizeQueue++ >= blockingQueue.size())
                throw new RuntimeException("Broken response");

            blockingQueue.add(rpcResponse);
            rpcResponse = blockingQueue.poll(TIMEOUT, TimeUnit.SECONDS);
        }

        if (rpcResponse.getException() != null)
            throw rpcResponse.getException();
        return rpcResponse.getResult();
    }

    /**
     * Called by RpcClientHandler.
     * @param rpcResponse the response packet from RpcClientHandler (netty's input)
     */
    public void notifyResponse(RpcResponse rpcResponse) {
        blockingQueue.add(rpcResponse);
    }

    /**
     * Using system built-in uuid generator, return a random string for identification.
     * @return the uuid value
     */
    private String generateUid() {
        return UUID.randomUUID().toString();
    }
}
