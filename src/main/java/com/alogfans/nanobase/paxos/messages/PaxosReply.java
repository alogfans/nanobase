package com.alogfans.nanobase.paxos.messages;

import java.io.Serializable;

/**
 * Created by Alogfans on 2015/10/3.
 */
public class PaxosReply implements Serializable {
    public boolean result;
    public int sessionId;
    public Object value;
}
