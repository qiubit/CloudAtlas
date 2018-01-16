package pl.edu.mimuw.cloudatlas.messages;

import pl.edu.mimuw.cloudatlas.model.PathName;
import pl.edu.mimuw.cloudatlas.model.ValueContact;

import java.io.Serializable;
import java.net.InetAddress;
import java.util.ArrayList;
import java.util.HashSet;

public class GetGossipMetadataResponseMessage extends SerializedMessage implements Serializable {
    public final Integer levelNum;
    public final PathName levelPath;
    public final HashSet<InetAddress> contacts;

    public GetGossipMetadataResponseMessage(Integer levelNum, PathName levelPath, HashSet<InetAddress> contacts) {
        this.levelNum = levelNum;
        this.levelPath = levelPath;
        this.contacts = contacts;
    }

    @Override
    public Message handle(MessageHandler m) {
        return m.handleMessage(this);
    }
}
