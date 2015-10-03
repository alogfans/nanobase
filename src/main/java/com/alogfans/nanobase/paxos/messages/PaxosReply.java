package com.alogfans.nanobase.paxos.messages;

/**
 * Created by Alogfans on 2015/10/3.
 */
public class PaxosReply {
    public boolean result;
    public int sessionId;
    public Object value;
}
