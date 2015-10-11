package com.alogfans.nanobase.kvpaxos;

import com.alogfans.nanobase.engine.Engine;
import com.alogfans.nanobase.engine.HashMapEngine;
import com.alogfans.nanobase.paxos.Paxos;
import com.alogfans.nanobase.rpc.server.Provider;
import com.alogfans.nanobase.rpc.server.RpcServer;

import java.util.concurrent.locks.ReentrantLock;

/**
 * Created by Alogfans on 2015/10/11.
 */
public class NanoBaseServer implements NanoBaseServerRpc {
    private ReentrantLock lock;         // for synchronous
    private RpcServer rpcServer;        // will working since initialization.
    private Paxos paxos;

    private Engine engineInstance;      // maintain the own key/value db
    private Engine lastOperationUUID;
    private Engine lastOperation;       // for each peer, maintain last operation invoked
    private int completedOperationId;   // all operations before it are completely sync.

    public NanoBaseServer(String[] peers, int[] paxosPorts, int servicePort, int current) {
        this.lock = new ReentrantLock();
        this.rpcServer = new RpcServer(servicePort);
        this.paxos = new Paxos(peers, paxosPorts, current);
        this.engineInstance = new HashMapEngine();
        this.lastOperationUUID = new HashMapEngine();
        this.lastOperation = new HashMapEngine();

        initializeRpcServer();
    }

    public void stop() {
        rpcServer.stop();
        paxos.stop();
    }

    private void initializeRpcServer() {
        Provider provider = new Provider()
                .setInterfaceClazz(NanoBaseServerRpc.class)
                .setInstance(this);
        rpcServer.addProvider(provider);

        try {
            rpcServer.run();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // The following implemented RPC interface
    public KvReply get(String key, String uuid, String clientId) {
        KvReply result;
        lock.lock();
        result = perform(
                new KvOperation(KvOperation.Command.GetOperation, key, null, uuid, clientId));

        lock.unlock();
        return result;
    }

    public KvReply put(String key, String value, String uuid, String clientId) {
        KvReply result;
        lock.lock();
        result = perform(new KvOperation
                (KvOperation.Command.PutOperation, key, value, uuid, clientId));

        lock.unlock();
        return result;
    }

    private KvReply perform(KvOperation operation) {
        final int MAX_ITERATIONS = 10000;
        KvReply reply = new KvReply();
        for (int i = 0; i < MAX_ITERATIONS; i++) {
            // So it have been processed yet, do not advance to make
            // at-most-once semantic
            String last = lastOperationUUID.get(operation.clientId);
            if (last != null && last.compareTo(operation.uuid) == 0) {
                reply.status = KvReply.Status.Success;
                reply.payload = lastOperation.get(operation.clientId);
                return reply;
            }

            int instanceId = completedOperationId + 1;
            Paxos.Status status = paxos.getStatus(instanceId);
            KvOperation result = null;

            if (status.accepted) {
                result = (KvOperation) status.value;
            } else {
                paxos.start(instanceId, operation);
                result = waitAgreement(instanceId);
                // If empty, please skip the apply operation
                if (result == null)
                    continue;
            }
            // Apply (result, instanceId);
            apply(instanceId, result);
            if (operation.uuid.compareTo(result.uuid) == 0) {
                reply.status = KvReply.Status.Success;
                reply.payload = lastOperation.get(operation.clientId);
                return reply;
            }
        }
        reply.status = KvReply.Status.InternalError;
        return reply;
    }

    private KvOperation waitAgreement(int instanceId) {
        int timeout = 10;
        while (timeout < 10 * 1000) {
            Paxos.Status status = paxos.getStatus(instanceId);
            if (status.accepted)
                return (KvOperation) status.value;

            try {
                Thread.sleep(timeout);
            } catch (InterruptedException e) {
                // Do nothing
            }

            timeout *= 2;
        }
        return null;
    }

    private void apply(int instanceId, KvOperation operation) {
        if (engineInstance.containsKey(operation.key)) {
            lastOperation.put(operation.clientId, engineInstance.get(operation.key));
        } else {
            lastOperation.put(operation.clientId, null);
        }

        lastOperationUUID.put(operation.clientId, operation.uuid);

        if (operation.command == KvOperation.Command.PutOperation) {
            engineInstance.put(operation.key, operation.value);
        }

        completedOperationId++;
        paxos.done(instanceId);
    }
}
