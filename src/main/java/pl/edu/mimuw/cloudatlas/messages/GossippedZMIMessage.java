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

    private boolean localIsSender = true;
    private long tsa;
    private long tsb;
    private long tra;
    private long trb;


    public GossippedZMIMessage(HashMap<String, ZMI> gossippedZmi,
                               HashMap<Attribute, QueryInformation> queries,
                               HashMap<String, HashSet<InetAddress>> contacts) {
        this.gossippedZmi = gossippedZmi;
        this.queries = queries;
        this.contacts = contacts;
    }

    public void setTimestamps(long tsa, long tsb, long tra, long trb) {
        this.tsa = tsa;
        this.tsb = tsb;
        this.tra = tra;
        this.trb = trb;
    }

    public HashMap<String, Long> getTimestamps() {
        HashMap<String, Long> ret = new HashMap<>();
        ret.put("tsa", this.tsa);
        ret.put("tsb", this.tsb);
        ret.put("tra", this.tra);
        ret.put("trb", this.trb);
        return ret;
    }

    public void setLocalIsSender(boolean localIsSender) {
        this.localIsSender = localIsSender;
    }

    public boolean getLocalIsSender() {
        return this.localIsSender;
    }

    @Override
    public Message handle(MessageHandler m) {
        return m.handleMessage(this);
    }
}
