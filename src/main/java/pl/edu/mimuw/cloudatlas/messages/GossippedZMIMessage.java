package pl.edu.mimuw.cloudatlas.messages;

import pl.edu.mimuw.cloudatlas.model.Attribute;
import pl.edu.mimuw.cloudatlas.model.QueryInformation;
import pl.edu.mimuw.cloudatlas.model.ZMI;

import java.io.Serializable;
import java.util.HashMap;

public class GossippedZMIMessage extends SerializedMessage implements Serializable {
    public final HashMap<String, ZMI> gossippedZmi;
    public final HashMap<Attribute, QueryInformation> queries;

    public GossippedZMIMessage(HashMap<String, ZMI> gossippedZmi, HashMap<Attribute, QueryInformation> queries) {
        this.gossippedZmi = gossippedZmi;
        this.queries = queries;
    }

    @Override
    public Message handle(MessageHandler m) {
        return m.handleMessage(this);
    }
}
