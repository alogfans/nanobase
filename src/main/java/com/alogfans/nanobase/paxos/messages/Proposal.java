package com.alogfans.nanobase.paxos.messages;

/**
 * Created by Alogfans on 2015/10/3.
 */
public class Proposal {
    public Proposal() {
        this.sessionId = 0;
        this.value = null;
    }

    public int sessionId;
    public Object value;
}
