package pl.edu.mimuw.cloudatlas.messages;

import pl.edu.mimuw.cloudatlas.model.Attribute;
import pl.edu.mimuw.cloudatlas.model.QueryInformation;
import pl.edu.mimuw.cloudatlas.model.ValueContact;
import pl.edu.mimuw.cloudatlas.model.ZMI;

import java.io.Serializable;
import java.net.InetAddress;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;

public class GossipTransactionRemoteZMIMessage extends SerializedMessage implements Serializable {
    public final String gossipLevel;
    public final HashMap<String, ZMI> relevantZmis;
    public final HashMap<String, HashSet<InetAddress>> contacts;
    public final HashMap<Attribute, QueryInformation> queries;

    public GossipTransactionRemoteZMIMessage(String gossipLevel,
                                             HashMap<String, ZMI> relevantZmis,
                                             HashMap<String, HashSet<InetAddress>> contacts,
                                             HashMap<Attribute, QueryInformation> queries) {
        this.gossipLevel = gossipLevel;
        this.relevantZmis = relevantZmis;
        this.contacts = contacts;
        this.queries = queries;
    }

    @Override
    public Message handle(MessageHandler m) {
        return m.handleMessage(this);
    }
}
