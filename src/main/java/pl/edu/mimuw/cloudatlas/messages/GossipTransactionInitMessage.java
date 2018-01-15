package pl.edu.mimuw.cloudatlas.messages;

import java.io.Serializable;

public class GossipTransactionInitMessage extends SerializedMessage implements Serializable {
    public final String gossipLevel;

    public GossipTransactionInitMessage(String gossipLevel) {
        this.gossipLevel = gossipLevel;
    }

    @Override
    public Message handle(MessageHandler m) {
        return m.handleMessage(this);
    }
}
