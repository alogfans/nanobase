package com.alogfans.nanobase.kvpaxos;

import java.io.Serializable;

/**
 * Created by Alogfans on 2015/10/11.
 */
public class KvReply implements Serializable {
    public enum Status {
        Success, KeyNotFound, InternalError
    }

    public KvReply() {
        status = Status.InternalError;
        payload = null;
    }

    public Status status;
    public String payload;
}
