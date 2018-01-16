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

public class GossippedZMIMessage extends SerializedMessage implements Serializable {
    public final HashMap<String, ZMI> gossippedZmi;
    public final HashMap<Attribute, QueryInformation> queries;
    public final HashMap<String, HashSet<InetAddress>> contacts;

    public GossippedZMIMessage(HashMap<String, ZMI> gossippedZmi,
                               HashMap<Attribute, QueryInformation> queries,
                               HashMap<String, HashSet<InetAddress>> contacts) {
        this.gossippedZmi = gossippedZmi;
        this.queries = queries;
        this.contacts = contacts;
    }

    @Override
    public Message handle(MessageHandler m) {
        return m.handleMessage(this);
    }
}
