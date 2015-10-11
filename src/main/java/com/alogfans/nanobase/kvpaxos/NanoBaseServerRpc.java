package com.alogfans.nanobase.kvpaxos;

/**
 * Created by Alogfans on 2015/10/11.
 */
public interface NanoBaseServerRpc {
    /**
     * Clients ask servers to get the value corresponding to key.
     * @param key the key
     * @param uuid to identify the client requests
     * @param clientId according to the consistent config file, the index of current workstation
     * @return the corresponding value, otherwise throw an KeyNotFound exception
     */
    KvReply get(String key, String uuid, String clientId);

    /**
     * Clients ask servers to insert the value corresponding to key. Note that the key may have
     * status before this invocation.
     *
     * @param key the key
     * @param value the value
     * @param needPreviousValue see also return
     * @param uuid to identify the client requests
     * @param clientId according to the consistent config file, the index of current workstation
     * @return if needPreviousValue is set and there is a previous value, return it. otherwise
     * return null
     */
    KvReply put(String key, String value, boolean needPreviousValue, String uuid, String clientId);
}
