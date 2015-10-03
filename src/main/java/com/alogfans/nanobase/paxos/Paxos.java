package com.alogfans.nanobase.paxos;

import com.alogfans.nanobase.paxos.messages.PaxosArgs;
import com.alogfans.nanobase.paxos.messages.PaxosReply;

/**
 * Paxos library, to be included in an application.
 * Created by Alogfans on 2015/10/2. (Based on MIT 6.824)
 */
public interface Paxos {
    /**
     * Invoke a PREPARE rpc function. thus proposals will ask acceptors to
     * complete stage one of paxos trip.
     * @param paxosArgs property instance id determines the topic (again
     *                  it's MULTI-PAXOS protocol, allowing several proposals)
     *                  and session id. the value argument is useless.
     * @return three elements, including response (ACK or NAK, depending on
     * session id) and last accepted proposal (if it have).
     */
    PaxosReply prepare(PaxosArgs paxosArgs);

    /**
     * Invoke a ACCEPT rpc function. thus proposals will ask acceptors to
     * complete stage two of paxos trip.
     * @param paxosArgs property instance id determines the topic (again
     *                  it's MULTI-PAXOS protocol, allowing several proposals)
     *                  and session id. the value argument stores the expected
     *                  value.
     * @return if according to promise (later proposal were prepared), it will
     * reject the accept request. Otherwise will accept it and update acceptor's
     * particular information.
     */
    boolean accept(PaxosArgs paxosArgs);
    /**
     * Invoke a COMMIT rpc function. because proposals complete full operation
     * and chosen a value, now commit it for broadcasting.
     * @param paxosArgs property instance id determines the topic (again
     *                  it's MULTI-PAXOS protocol, allowing several proposals)
     *                  and session id. the value argument stores the expected
     *                  value.
     */
    void broadcast(PaxosArgs paxosArgs);

}
