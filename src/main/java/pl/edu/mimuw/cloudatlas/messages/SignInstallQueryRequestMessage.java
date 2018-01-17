package pl.edu.mimuw.cloudatlas.messages;

import pl.edu.mimuw.cloudatlas.model.Query;

import java.io.Serializable;

public class SignInstallQueryRequestMessage extends SerializedMessage implements Serializable {
    public final Query query;

    public SignInstallQueryRequestMessage(Query query) {
        this.query = query;
    }

    @Override
    public Message handle(MessageHandler m) {
        return m.handleMessage(this);
    }
}
