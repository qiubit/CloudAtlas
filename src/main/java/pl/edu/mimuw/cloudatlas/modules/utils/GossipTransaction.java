package pl.edu.mimuw.cloudatlas.modules.utils;

import pl.edu.mimuw.cloudatlas.model.ZMI;

import java.util.HashMap;

public class GossipTransaction {
    private GossipTransaction.State state;
    private String initiatorHostname;
    private Long timestamp;
    private String gossipLevel;
    private HashMap<String, ZMI> remoteZmis = null;

    public enum State {
        REQUESTED,
        LOCAL_ZMI_SENT,
    }

    public GossipTransaction(String initiatorHostname, String gossipLevel) {
        this.initiatorHostname = initiatorHostname;
        this.timestamp = System.currentTimeMillis();
        this.state = GossipTransaction.State.REQUESTED;
        this.gossipLevel = gossipLevel;
    }

    public String getInitiatorHostname() {
        return initiatorHostname;
    }

    public Long getTimestamp() {
        return timestamp;
    }

    public GossipTransaction.State getState() {
        return state;
    }

    public String getGossipLevel() {
        return gossipLevel;
    }

    public void markLocalZmiSent() {
        this.state = GossipTransaction.State.LOCAL_ZMI_SENT;
    }

    public void setRemoteZmis(HashMap<String, ZMI> remoteZmis) {
        this.remoteZmis = remoteZmis;
    }

    public HashMap<String, ZMI> getRemoteZmis() {
        return this.remoteZmis;
    }
}
