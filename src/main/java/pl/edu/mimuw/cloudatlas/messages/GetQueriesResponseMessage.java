package pl.edu.mimuw.cloudatlas.messages;

import pl.edu.mimuw.cloudatlas.model.Attribute;
import pl.edu.mimuw.cloudatlas.model.QueryInformation;

import java.io.Serializable;
import java.util.HashMap;

public class GetQueriesResponseMessage extends SerializedMessage implements Serializable {
    public final HashMap<Attribute, QueryInformation> queries;

    public GetQueriesResponseMessage(HashMap<Attribute, QueryInformation> queries) {
        this.queries = queries;
    }

    @Override
    public Message handle(MessageHandler m) {
        return m.handleMessage(this);
    }
}
