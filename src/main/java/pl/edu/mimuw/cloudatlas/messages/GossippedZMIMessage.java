package pl.edu.mimuw.cloudatlas.messages;

import pl.edu.mimuw.cloudatlas.model.ZMI;

import java.io.Serializable;
import java.util.HashMap;

public class GossippedZMIMessage extends SerializedMessage implements Serializable {
    public final HashMap<String, ZMI> gossippedZmi;

    public GossippedZMIMessage(HashMap<String, ZMI> gossippedZmi) {
        this.gossippedZmi = gossippedZmi;
    }

    @Override
    public Message handle(MessageHandler m) {
        return m.handleMessage(this);
    }
}
