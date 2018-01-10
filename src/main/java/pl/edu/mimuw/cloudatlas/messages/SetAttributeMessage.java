package pl.edu.mimuw.cloudatlas.messages;

import pl.edu.mimuw.cloudatlas.model.Attribute;
import pl.edu.mimuw.cloudatlas.model.Value;

import java.io.Serializable;

public class SetAttributeMessage extends SerializedMessage implements Serializable {
    private Attribute attribute;
    private Value value;

    public SetAttributeMessage(Attribute attribute, Value value) {
        this.attribute = attribute;
        this.value = value;
    }

    public Attribute getAttribute() {
        return attribute;
    }

    public Value getValue() {
        return value;
    }

    public void setReceiverQueueName(String qName) { this.receiverQueueName = qName; }

    @Override
    public Message handle(MessageHandler m) {
        return m.handleMessage(this);
    }
}
