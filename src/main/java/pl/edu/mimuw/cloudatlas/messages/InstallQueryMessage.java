package pl.edu.mimuw.cloudatlas.messages;

import pl.edu.mimuw.cloudatlas.model.Query;

import java.io.Serializable;

public class InstallQueryMessage extends SerializedMessage implements Serializable {
    public final Query query;
    public final byte[] signature;

    public InstallQueryMessage(Query query, byte[] signature) {
        this.query = query;
        this.signature = signature;
    }

    @Override
    public Message handle(MessageHandler m) {
        return m.handleMessage(this);
    }
}
