package pl.edu.mimuw.cloudatlas.messages;

import java.io.Serializable;

public class GetGossipMetadataRequestMessage extends SerializedMessage implements Serializable {
    public final Integer requestedLevelNum;

    public GetGossipMetadataRequestMessage(Integer requestedLevelNum) {
        this.requestedLevelNum = requestedLevelNum;
    }

    @Override
    public Message handle(MessageHandler m) {
        return m.handleMessage(this);
    }
}
