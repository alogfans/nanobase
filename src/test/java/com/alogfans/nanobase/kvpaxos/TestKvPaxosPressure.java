package com.alogfans.nanobase.kvpaxos;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

/**
 * Created by Alogfans on 2015/10/11.
 */
public class TestKvPaxosPressure {
    final int SERVERS = 2;
    final int CLIENTS = 1;

    NanoBaseServer[] servers = new NanoBaseServer[SERVERS];
    NanoBaseClient[] clients = new NanoBaseClient[CLIENTS];
    String[] peers = new String[SERVERS];
    int[] paxosPorts = new int[SERVERS];
    int[] kvPorts = new int[SERVERS];

    @Before
    public void before() {
        for (int i = 0; i < SERVERS; i++) {
            peers[i] = "localhost";
            paxosPorts[i] = 8080 + i;
            kvPorts[i] = 8000 + i;
        }

        for (int i = 0; i < SERVERS; i++) {
            servers[i] = new NanoBaseServer(peers, paxosPorts, kvPorts[i], i);
        }

        for (int i = 0; i < CLIENTS; i++) {
            clients[i] = new NanoBaseClient(peers, kvPorts, Integer.toString(i));
        }
    }

    @After
    public void after() {
        for (int i = 0; i < SERVERS; i++) {
            servers[i].stop();
        }
    }

    @Test
    public void testLotOfRW() {
        for (int i = 0; i < 100; i++) {
            clients[i % CLIENTS].put(Integer.toString(i), Integer.toString(i));
        }

        for (int i = 0; i < 100; i++) {
            Assert.assertEquals(clients[(i + 3) % CLIENTS].get(Integer.toString(i)), Integer.toString(i));
        }
    }
}
