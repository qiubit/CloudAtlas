package pl.edu.mimuw.cloudatlas.messages;

import pl.edu.mimuw.cloudatlas.model.Attribute;

import java.io.Serializable;

public class InstallQueryMessage extends SerializedMessage implements Serializable {
    public final Attribute name;
    public final String query;

    public InstallQueryMessage(Attribute name, String query) {
        this.name = name;
        this.query = query;
    }

    @Override
    public Message handle(MessageHandler m) {
        return m.handleMessage(this);
    }
}
