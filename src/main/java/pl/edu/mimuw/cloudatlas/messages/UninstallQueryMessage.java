package pl.edu.mimuw.cloudatlas.messages;

import pl.edu.mimuw.cloudatlas.model.Attribute;

import java.io.Serializable;

public class UninstallQueryMessage extends SerializedMessage implements Serializable {
    public final Attribute name;

    public UninstallQueryMessage(Attribute name) {
        this.name = name;
    }

    @Override
    public Message handle(MessageHandler m) {
        return m.handleMessage(this);
    }
}
