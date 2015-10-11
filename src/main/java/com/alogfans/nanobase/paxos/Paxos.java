package com.alogfans.nanobase.paxos;

import com.alogfans.nanobase.paxos.messages.PaxosArgs;
import com.alogfans.nanobase.paxos.messages.PaxosReply;
import com.alogfans.nanobase.paxos.messages.Proposal;
import com.alogfans.nanobase.rpc.client.RpcClient;
import com.alogfans.nanobase.rpc.server.Provider;
import com.alogfans.nanobase.rpc.server.RpcServer;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.ReentrantLock;

/**
 * PaxosRpc library, to be included in an application.
 * Created by Alogfans on 2015/10/2.
 */
public class Paxos implements PaxosRpc {
    private final int PAXOS_RPC_PORT = 8080;

    private ReentrantLock lock;         // for synchronous
    private RpcServer rpcServer;        // will working since initialization.
    private String[] peers;
    private int[] ports;
    private int current;                // index to current peer
    private int[] doneInstances;        // instances that have done with peer list
    private Map<Integer, PaxosInstance> instanceMap;

    // For Single-PC debug only
    // public boolean reliable = true;

    public Paxos(String[] peers, int[] ports, int current) {
        this.peers = peers;
        this.ports = ports;

        this.current = current;
        this.doneInstances = new int[peers.length];
        for (int i = 0; i < peers.length; i++)
            this.doneInstances[i] = -1;  // nothing is done

        this.lock = new ReentrantLock();
        this.rpcServer = new RpcServer(ports[current]);
        initializeRpcServer();
        this.instanceMap = new ConcurrentHashMap<Integer, PaxosInstance>();
    }

    private class PaxosInstance {
        public PaxosInstance(int instanceId) {
            this.instanceId = instanceId;
            this.value = null;
            this.accepted = false;
            this.prepared = 0;
            this.proposal = new Proposal();
        }

        public int instanceId;          // the id of instance, also the key in such map
        public Object value;            // current value (may be modified)
        public boolean accepted;        // whether completed stage two?
        public int prepared;            // the last prepared session
        public Proposal proposal;       // last accepted proposal
    }

    private void initializeRpcServer() {
        Provider provider = new Provider()
                .setInterfaceClazz(PaxosRpc.class)
                .setInstance(this);
        rpcServer.addProvider(provider);

        try {
            rpcServer.run();
        } catch (Exception e) {
            e.printStackTrace();
        }

        /*
        new Thread(new Runnable() {
            public void run() {
                try {
                    rpcServer.run();
                    while (true) {
                        if (!reliable) {
                            Random random = new Random();
                            if (random.nextInt(Integer.MAX_VALUE) < Integer.MAX_VALUE / 100) {
                                // shut down it by force
                                Thread.currentThread().stop();
                                break;
                            }
                        }
                        Thread.sleep(500);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                } finally {
                    // rpcServer.stop();
                }
            }
        }).start();
        */
    }

    // The following functions are rpc routines, DO NOT DIRECTLY USE THEM!
    public PaxosReply prepare(PaxosArgs paxosArgs) {
        // Note: current role is ACCEPTOR. Execute Stage 1(2).
        lock.lock();
        PaxosReply paxosReply = new PaxosReply();
        int instanceId = paxosArgs.instanceId;

        if (!instanceMap.containsKey(instanceId)) {
            // have not seen proposals before
            PaxosInstance newInstance = new PaxosInstance(instanceId);
            instanceMap.put(instanceId, newInstance);

            paxosReply.result = true;
        } else if (instanceMap.get(instanceId).prepared < paxosArgs.sessionId) {
            // not newer session that prepared.
            paxosReply.result = true;
        }

        // write back the acceptor's last accept proposal if have it
        if (paxosReply.result) {
            paxosReply.sessionId = instanceMap.get(instanceId).proposal.sessionId;
            paxosReply.value = instanceMap.get(instanceId).proposal.value;
            instanceMap.get(instanceId).prepared = paxosArgs.sessionId;
        }

        lock.unlock();
        return paxosReply;
    }

    public boolean accept(PaxosArgs paxosArgs) {
        lock.lock();
        boolean reply = false;
        int instanceId = paxosArgs.instanceId;
        if (!instanceMap.containsKey(instanceId)) {
            PaxosInstance newInstance = new PaxosInstance(instanceId);
            instanceMap.put(instanceId, newInstance);
        }

        if (paxosArgs.sessionId >= instanceMap.get(instanceId).prepared) {
            // do accept: follow the promise
            instanceMap.get(instanceId).proposal.sessionId = paxosArgs.sessionId;
            instanceMap.get(instanceId).proposal.value = paxosArgs.value;
            instanceMap.get(instanceId).prepared = paxosArgs.sessionId;
            reply = true;
        }

        lock.unlock();
        return reply;
    }

    public void broadcast(PaxosArgs paxosArgs) {
        lock.lock();
        int instanceId = paxosArgs.instanceId;
        if (!instanceMap.containsKey(instanceId)) {
            PaxosInstance newInstance = new PaxosInstance(instanceId);
            instanceMap.put(instanceId, newInstance);
        }

        // update local storage
        instanceMap.get(instanceId).proposal.sessionId = paxosArgs.sessionId;
        instanceMap.get(instanceId).proposal.value = paxosArgs.value;
        instanceMap.get(instanceId).accepted = true;

        if (paxosArgs.invokerIndex != current)
            doneInstances[paxosArgs.invokerIndex] = paxosArgs.doneCurrent;

        lock.unlock();
    }

    // the following functions are for final users.
    public boolean doPrepare(int instanceId, Object value, Proposal finalProposal) {
        Proposal lastAccepted = new Proposal();
        int sessionId = generateSessionId();
        int acknowledged = 0;

        PaxosArgs paxosArgs = new PaxosArgs();
        paxosArgs.instanceId = instanceId;
        paxosArgs.sessionId = sessionId;
        paxosArgs.value = value;
        paxosArgs.invokerIndex = current;
        paxosArgs.doneCurrent = doneInstances[current];

        for (int i = 0; i < peers.length; i++) {
            PaxosReply paxosReply = null;
            if (i == current)
                paxosReply = prepare(paxosArgs);
            else {
                // Via Rpc function. Note that it's short connection
                RpcClient rpcClient = new RpcClient(peers[i], ports[i]);
                PaxosRpc handler = (PaxosRpc) rpcClient.createInvoker()
                        .setInterfaceClazz(PaxosRpc.class)
                        .getInstance();
                try {
                    rpcClient.run();
                    paxosReply = handler.prepare(paxosArgs);
                    rpcClient.stop();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

            if (paxosReply != null && paxosReply.result) {
                if (paxosReply.sessionId > lastAccepted.sessionId) {
                    lastAccepted.sessionId = paxosReply.sessionId;
                    lastAccepted.value = paxosReply.value;
                }
                acknowledged++;
            }
        }

        // said the majority
        if (acknowledged > peers.length / 2) {
            finalProposal.sessionId = sessionId;
            if (lastAccepted.value == null)
                finalProposal.value = value;
            else
                finalProposal.value = lastAccepted.value;

            return true;
        }
        return false;
    }

    public boolean doAccept(int instanceId, Proposal finalProposal) {
        int acknowledged = 0;

        PaxosArgs paxosArgs = new PaxosArgs();
        paxosArgs.instanceId = instanceId;
        paxosArgs.sessionId = finalProposal.sessionId;
        paxosArgs.value = finalProposal.value;
        paxosArgs.invokerIndex = current;
        paxosArgs.doneCurrent = doneInstances[current];

        for (int i = 0; i < peers.length; i++) {
            boolean reply = false;

            if (i == current)
                reply = accept(paxosArgs);
            else {
                // Via Rpc function. Note that it's short connection
                RpcClient rpcClient = new RpcClient(peers[i], ports[i]);
                PaxosRpc handler = (PaxosRpc) rpcClient.createInvoker()
                        .setInterfaceClazz(PaxosRpc.class)
                        .getInstance();
                try {
                    rpcClient.run();
                    reply = handler.accept(paxosArgs);
                    rpcClient.stop();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

            if (reply) {
                acknowledged++;
            }
        }
        return (acknowledged > peers.length / 2);
    }

    public void doBroadcast(int instanceId, Proposal finalProposal) {
        PaxosArgs paxosArgs = new PaxosArgs();
        paxosArgs.instanceId = instanceId;
        paxosArgs.sessionId = finalProposal.sessionId;
        paxosArgs.value = finalProposal.value;
        paxosArgs.invokerIndex = current;
        paxosArgs.doneCurrent = doneInstances[current];

        for (int i = 0; i < peers.length; i++) {
            if (i == current)
                broadcast(paxosArgs);
            else {
                // Via Rpc function. Note that it's short connection
                RpcClient rpcClient = new RpcClient(peers[i], ports[i]);
                PaxosRpc handler = (PaxosRpc) rpcClient.createInvoker()
                        .setInterfaceClazz(PaxosRpc.class)
                        .getInstance();
                try {
                    rpcClient.run();
                    handler.broadcast(paxosArgs);
                    rpcClient.stop();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
    }

    public void proposer(int instanceId, Object value) {
        while (true) {
            Proposal finalProposal = new Proposal();
            if (!doPrepare(instanceId, value, finalProposal))
                continue;
            if (!doAccept(instanceId, finalProposal))
                continue;
            doBroadcast(instanceId, finalProposal);
            // System.out.printf("All fun, done!");
            return;
        }
    }

    private int generateSessionId() {
        // support 256 agents currently
        int timestamp = (int) (System.nanoTime() & 0x00ffffff) >> 8;
        return timestamp + current;
    }

    // The following APIS are available to end user
    public void start(final int instanceId, final Object value) {
        new Thread(new Runnable() {
            public void run() {
                if (instanceId < getMin())
                    return;
                proposer(instanceId, value);
            }
        }).start();
    }

    public int getMin() {
        lock.lock();
        int minimal = 0;
        for (int i = 0; i < doneInstances.length; i++) {
            if (minimal > doneInstances[i])
                minimal = doneInstances[i];
        }

        for (int instanceId : instanceMap.keySet()) {
            if (instanceId <= minimal && instanceMap.get(instanceId).accepted) {
                instanceMap.remove(instanceId);
            }
        }

        lock.unlock();
        return minimal + 1;
    }

    public int getMax() {
        int largest = 0;
        for (int instanceId : instanceMap.keySet()) {
            if (largest < instanceId) {
                largest = instanceId;
            }
        }
        return largest;
    }

    public void done(int instanceId) {
        if (doneInstances[current] < instanceId)
            doneInstances[current] = instanceId;
    }

    public class Status {
        public Status(boolean accepted, Object value) {
            this.accepted = accepted;
            this.value = value;
        }

        public boolean accepted;
        public Object value;
    }

    public Status getStatus(int instanceId) {
        Status status = new Status(false, null);

        if (instanceId < getMin())
            return status;
        lock.lock();

        if (!instanceMap.containsKey(instanceId)) {
            lock.unlock();
            return status;
        }

        status.accepted = instanceMap.get(instanceId).accepted;
        status.value = instanceMap.get(instanceId).proposal.value;

        lock.unlock();
        return status;
    }

    public void stop() {
        rpcServer.stop();
    }
}

