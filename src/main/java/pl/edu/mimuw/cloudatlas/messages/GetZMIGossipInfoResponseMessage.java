package pl.edu.mimuw.cloudatlas.messages;

import pl.edu.mimuw.cloudatlas.model.ValueContact;
import pl.edu.mimuw.cloudatlas.model.ZMI;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;

public class GetZMIGossipInfoResponseMessage extends SerializedMessage implements Serializable {
    public final HashMap<String, ZMI> relevantZMIs;
    public final ArrayList<ValueContact> fallbackContacts;

    public GetZMIGossipInfoResponseMessage(HashMap<String, ZMI> relevantZMIs, ArrayList<ValueContact> contacts) {
        this.relevantZMIs = relevantZMIs;
        this.fallbackContacts = contacts;
    }

    @Override
    public Message handle(MessageHandler m) {
        return m.handleMessage(this);
    }
}
