package com.alogfans.nanobase.paxos;

import org.junit.Assert;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

/**
 * Created by Alogfans on 2015/10/4.
 */
public class TestPaxosDeaf {
    private final int MACHINE_COUNT = 6;

    private String[] peers = null;
    private PaxosRoutine[] paxosRoutines = null;

    @Before
    public void setup() {
        peers = new String[MACHINE_COUNT];
        paxosRoutines = new PaxosRoutine[MACHINE_COUNT];

        for (int i = 0; i < MACHINE_COUNT; i++) {
            peers[i] = "localhost";
        }

        for (int i = 0; i < MACHINE_COUNT; i++) {
            paxosRoutines[i] = new PaxosRoutine(peers, i);
        }
    }

    @After
    public void teardown() {
        for (int i = 0; i < MACHINE_COUNT; i++)
            paxosRoutines[i].shutdown();
    }

    public void wait(int timeout) {
        try {
            Thread.sleep(timeout);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void validate(int instanceId) {
        int currentTimeout = 10;
        while (currentTimeout < 10000) {
            wait(currentTimeout);
            currentTimeout *= 2;

            Object expected = null;
            int acknowledged = 0;
            for (int i = 0; i < MACHINE_COUNT; i++) {
                if (paxosRoutines[i].getStatus(instanceId).accepted) {
                    if (acknowledged == 0)
                        expected = paxosRoutines[i].getStatus(instanceId).value;

                    Assert.assertEquals(paxosRoutines[i].getStatus(instanceId).value, expected);
                    acknowledged++;
                }
            }
            // System.out.printf("Ack = %d\n", acknowledged);
            if (acknowledged > MACHINE_COUNT / 2)
                return;
        }
        // should not goes here!
        Assert.assertTrue(true);
    }

    @Test
    public void testDeafProposer() {
        paxosRoutines[0].start(0, "hello");
        validate(0);
        peers[0] = "";
        peers[MACHINE_COUNT - 1] = "";
        paxosRoutines[1].start(1, "goodbye");
        validate(1);
        paxosRoutines[0].start(1, "xxx");
        validate(1);
    }

    @Test
    public void testForgotten() {
        for (int i = 0; i < MACHINE_COUNT; i++) {
            Assert.assertTrue(paxosRoutines[i].getMin() <= 0);
        }

        paxosRoutines[0].start(0, "00");
        paxosRoutines[1].start(1, "11");
        paxosRoutines[2].start(2, "22");
        paxosRoutines[0].start(6, "66");
        paxosRoutines[1].start(7, "77");

        validate(0);
        for (int i = 0; i < MACHINE_COUNT; i++) {
            Assert.assertTrue(paxosRoutines[i].getMin() == 0);
        }

        validate(1);
        for (int i = 0; i < MACHINE_COUNT; i++) {
            Assert.assertTrue(paxosRoutines[i].getMin() == 0);
        }

        for (int i = 0; i < MACHINE_COUNT; i++) {
            paxosRoutines[i].done(0);
        }

        for (int i = 0; i < MACHINE_COUNT; i++) {
            paxosRoutines[i].done(1);
        }

        for (int i = 0; i < MACHINE_COUNT; i++) {
            paxosRoutines[i].start(8 + i, "xx");
        }

        boolean allok = false;
        for (int t = 0; t < 12; t++) {
            allok = true;
            for (int i = 0; i < MACHINE_COUNT; i++) {
                if (paxosRoutines[i].getMin() != 1)
                    allok = false;
            }

            if (allok)
                break;

            wait(1000);
        }

        Assert.assertTrue(allok);
    }
}
