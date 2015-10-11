package com.alogfans.nanobase.kvpaxos;

import java.io.Serializable;

/**
 * Created by Alogfans on 2015/10/11.
 */
public class KvOperation implements Serializable {
    public KvOperation(Command command, String key, String value, String uuid, String clientId) {
        this.command = command;
        this.key = key;
        this.value = value;
        this.uuid = uuid;
        this.clientId = clientId;
    }

    public enum Command {
        GetOperation, PutOperation
    }

    public Command command;
    public String key;
    public String value;
    public String uuid;
    public String clientId;
}
