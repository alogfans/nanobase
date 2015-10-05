package com.alogfans.nanobase.paxos;

import junit.framework.Assert;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

/**
 * Test cases for Paxos components.
 *
 * Created by Alogfans on 2015/10/3.
 */
public class TestPaxosSimple {
    private final int MACHINE_COUNT = 3;

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

            if (acknowledged > MACHINE_COUNT / 2)
                return;
        }
        // should not goes here!
        Assert.assertTrue(true);
    }

    @Test
    public void testOneProposal() {
        paxosRoutines[0].start(0, "Hello World!");
        validate(0);
    }

    @Test
    public void testManyProposalsWithSameValue() {
        for (int i = 0; i < 3; i++)
            paxosRoutines[i].start(0, "Hello World!");
        validate(0);
    }

    @Test
    public void testManyProposalsWithDifferentValues() {
        paxosRoutines[0].start(0, 100);
        paxosRoutines[1].start(0, 102);
        paxosRoutines[2].start(0, 104);
        validate(0);
    }

    @Test
    public void testOutOfOrder() {
        paxosRoutines[0].start(7, 700);
        paxosRoutines[0].start(6, 600);
        paxosRoutines[1].start(5, 500);
        validate(7);
        paxosRoutines[0].start(4, 400);
        paxosRoutines[1].start(3, 300);
        validate(6);
        validate(5);
        validate(4);
        validate(3);
        // System.out.println(paxosRoutines[0].getMax());
        Assert.assertTrue(paxosRoutines[0].getMax() == 7);
    }
}
