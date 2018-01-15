package pl.edu.mimuw.cloudatlas.messages;

import pl.edu.mimuw.cloudatlas.model.Attribute;
import pl.edu.mimuw.cloudatlas.model.QueryInformation;
import pl.edu.mimuw.cloudatlas.model.ValueContact;
import pl.edu.mimuw.cloudatlas.model.ZMI;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;

public class GossipTransactionRemoteZMIMessage extends SerializedMessage implements Serializable {
    public final String gossipLevel;
    public final HashMap<String, ZMI> relevantZmis;
    public final ArrayList<ValueContact> fallbackContacts;
    public final HashMap<Attribute, QueryInformation> queries;

    public GossipTransactionRemoteZMIMessage(String gossipLevel,
                                             HashMap<String, ZMI> relevantZmis,
                                             ArrayList<ValueContact> contacts,
                                             HashMap<Attribute, QueryInformation> queries) {
        this.gossipLevel = gossipLevel;
        this.relevantZmis = relevantZmis;
        this.fallbackContacts = contacts;
        this.queries = queries;
    }

    @Override
    public Message handle(MessageHandler m) {
        return m.handleMessage(this);
    }
}
