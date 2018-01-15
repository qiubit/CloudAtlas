package pl.edu.mimuw.cloudatlas.messages;

import pl.edu.mimuw.cloudatlas.model.PathName;
import pl.edu.mimuw.cloudatlas.model.ValueContact;

import java.io.Serializable;
import java.util.ArrayList;

public class GetGossipMetadataResponseMessage extends SerializedMessage implements Serializable {
    public final Integer levelNum;
    public final PathName levelPath;
    public final ArrayList<ValueContact> contacts;

    public GetGossipMetadataResponseMessage(Integer levelNum, PathName levelPath, ArrayList<ValueContact> contacts) {
        this.levelNum = levelNum;
        this.levelPath = levelPath;
        this.contacts = contacts;
    }

    @Override
    public Message handle(MessageHandler m) {
        return m.handleMessage(this);
    }
}
