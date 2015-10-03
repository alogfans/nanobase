package com.alogfans.nanobase.paxos;

import com.alogfans.nanobase.paxos.messages.PaxosArgs;
import com.alogfans.nanobase.paxos.messages.PaxosReply;
import com.alogfans.nanobase.paxos.messages.Proposal;
import com.alogfans.nanobase.rpc.client.RpcClient;
import com.alogfans.nanobase.rpc.server.RpcServer;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.locks.ReentrantLock;

/**
 * Paxos library, to be included in an application.
 * Created by Alogfans on 2015/10/2.
 */
public class PaxosRoutine implements Paxos {
    private final int PAXOS_RPC_PORT = 8080;

    private ReentrantLock lock;         // for synchronous
    private RpcServer rpcServer;        // will working since initialization.
    private String[] peers;
    private int current;                // index to current peer
    private int[] doneSessions;         // session that have done with peer list
    private Map<Integer, PaxosInstance> instanceMap;

    public PaxosRoutine(String[] peers, int current) {
        this.peers = peers;
        this.current = current;
        this.doneSessions = new int[peers.length];
        for (int i = 0; i < peers.length; i++)
            this.doneSessions[i] = -1;  // nothing is done

        this.lock = new ReentrantLock();
        this.rpcServer = new RpcServer(PAXOS_RPC_PORT);
        this.instanceMap = new HashMap<Integer, PaxosInstance>();
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

    public void commit(PaxosArgs paxosArgs) {

    }

    // the following functions are for final users.
    public boolean doPrepare(int instanceId, Object value, Proposal finalProposal) {
        Proposal lastAccepted = new Proposal();
        int sessionId = generateSessionId();
        int acknowledged = 0;

        PaxosArgs paxosArgs = new PaxosArgs();
        paxosArgs.instanceId = instanceId;
        paxosArgs.sessionId = sessionId;

        for (int i = 0; i < peers.length; i++) {
            PaxosReply paxosReply = null;
            if (i == current)
                paxosReply = prepare(paxosArgs);
            else {
                // Via Rpc function. Note that it's short connection
                RpcClient rpcClient = new RpcClient(peers[i], PAXOS_RPC_PORT);
                Paxos handler = (Paxos) rpcClient.createInvoker()
                        .setInterfaceClazz(Paxos.class)
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

    private int generateSessionId() {
        // support 256 agents currently
        int timestamp = (int) (System.nanoTime() & 0x00ffffff) >> 8;
        return timestamp + current;
    }
}
