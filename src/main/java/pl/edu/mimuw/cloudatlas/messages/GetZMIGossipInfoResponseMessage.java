package pl.edu.mimuw.cloudatlas.messages;

import pl.edu.mimuw.cloudatlas.model.Attribute;
import pl.edu.mimuw.cloudatlas.model.QueryInformation;
import pl.edu.mimuw.cloudatlas.model.ValueContact;
import pl.edu.mimuw.cloudatlas.model.ZMI;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;

public class GetZMIGossipInfoResponseMessage extends SerializedMessage implements Serializable {
    public final String gossippedLevel;
    public final HashMap<String, ZMI> relevantZMIs;
    public final ArrayList<ValueContact> fallbackContacts;
    public final HashMap<Attribute, QueryInformation> queries;

    public GetZMIGossipInfoResponseMessage(String gossippedLevel,
                                           HashMap<String, ZMI> relevantZMIs,
                                           ArrayList<ValueContact> contacts,
                                           HashMap<Attribute, QueryInformation> queries) {
        this.gossippedLevel = gossippedLevel;
        this.relevantZMIs = relevantZMIs;
        this.fallbackContacts = contacts;
        this.queries = queries;
    }

    @Override
    public Message handle(MessageHandler m) {
        return m.handleMessage(this);
    }
}
