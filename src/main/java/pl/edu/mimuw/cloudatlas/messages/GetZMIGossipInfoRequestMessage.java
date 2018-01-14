package pl.edu.mimuw.cloudatlas.messages;

import java.io.Serializable;

public class GetZMIGossipInfoRequestMessage extends SerializedMessage implements Serializable {
    public final String requestedLevel;

    public GetZMIGossipInfoRequestMessage(String level) {
        this.requestedLevel = level;
    }

    @Override
    public Message handle(MessageHandler m) {
        return m.handleMessage(this);
    }
}
