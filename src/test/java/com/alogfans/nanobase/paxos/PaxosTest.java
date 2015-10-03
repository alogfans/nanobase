package com.alogfans.nanobase.paxos;

/**
 * Created by Alogfans on 2015/10/3.
 */
public class PaxosTest {
    public static void main(String[] args) throws Exception {
        String[] peers = new String[3];
        PaxosRoutine[] paxosRoutines = new PaxosRoutine[3];

        for (int i = 0; i < 3; i++) {
            peers[i] = "localhost";
        }

        for (int i = 0; i < 3; i++) {
            paxosRoutines[i] = new PaxosRoutine(peers, i);
        }

        paxosRoutines[0].start(0, "Hello World!");
        //Thread.sleep(1000);
        //System.out.print(paxosRoutines[1].getStatus(0).accepted);
        //System.out.print(paxosRoutines[2].getStatus(0).accepted);
    }
}
