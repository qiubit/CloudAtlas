package pl.edu.mimuw.cloudatlas.messages;

import java.io.Serializable;

public class GossipTransactionInitMessage extends SerializedMessage implements Serializable {
    public final String gossipLevel;
    private long tsa;

    public GossipTransactionInitMessage(String gossipLevel) {
        this.gossipLevel = gossipLevel;
    }

    public void setTsa() {
        tsa = System.currentTimeMillis();
    }

    public long getTsa() {
        return this.tsa;
    }

    @Override
    public Message handle(MessageHandler m) {
        return m.handleMessage(this);
    }
}
