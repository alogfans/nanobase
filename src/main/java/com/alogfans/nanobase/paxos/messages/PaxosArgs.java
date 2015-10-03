package com.alogfans.nanobase.paxos.messages;

/**
 * In rpc transactions, we will use two standard packet format.
 *
 * Created by Alogfans on 2015/10/3.
 */
public class PaxosArgs {
    public int instanceId;
    public int sessionId;
    public Object value;

    public int invokerIndex;
    public int doneCurrent;
}
