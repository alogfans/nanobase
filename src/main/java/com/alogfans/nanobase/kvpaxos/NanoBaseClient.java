package com.alogfans.nanobase.kvpaxos;

import com.alogfans.nanobase.rpc.client.RpcClient;

import java.util.UUID;

/**
 * Created by Alogfans on 2015/10/11.
 */
public class NanoBaseClient {
    private String clientId;
    private String[] servers;
    private int[] ports;

    public NanoBaseClient(String[] servers, int[] ports, String clientId) {
        this.servers = servers;
        this.clientId = clientId;
        this.ports = ports;
    }

    public String get(String key) {
        final int MAX_ITERATIONS = 1000;
        String uuid = generateUid();

        for (int i = 0; i < MAX_ITERATIONS * servers.length; i++) {
            int serverId = i % servers.length;

            RpcClient rpcClient = new RpcClient(servers[serverId], ports[serverId]);
            KvReply reply = null;
            NanoBaseServerRpc handler = (NanoBaseServerRpc) rpcClient.createInvoker()
                    .setInterfaceClazz(NanoBaseServerRpc.class)
                    .getInstance();
            try {
                rpcClient.run();
                reply = handler.get(key, uuid, clientId);
                rpcClient.stop();
            } catch (Exception e) {
                e.printStackTrace();
            }

            if (reply != null && reply.status == KvReply.Status.Success) {
                return reply.payload;
            }
        }

        return null;
    }

    public String put(String key, String value) {
        final int MAX_ITERATIONS = 1000;
        String uuid = generateUid();

        for (int i = 0; i < MAX_ITERATIONS * servers.length; i++) {
            int serverId = i % servers.length;

            RpcClient rpcClient = new RpcClient(servers[serverId], ports[serverId]);
            KvReply reply = null;
            NanoBaseServerRpc handler = (NanoBaseServerRpc) rpcClient.createInvoker()
                    .setInterfaceClazz(NanoBaseServerRpc.class)
                    .getInstance();
            try {
                rpcClient.run();
                reply = handler.put(key, value, uuid, clientId);
                rpcClient.stop();
            } catch (Exception e) {
                e.printStackTrace();
            }

            if (reply != null && reply.status == KvReply.Status.Success) {
                return reply.payload;
            }
        }

        return null;
    }

    /**
     * Using system built-in uuid generator, return a random string for identification.
     * @return the uuid value
     */
    private String generateUid() {
        return UUID.randomUUID().toString();
    }
}
