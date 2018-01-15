package pl.edu.mimuw.cloudatlas.messages;

import pl.edu.mimuw.cloudatlas.model.AttributesMap;

import java.io.Serializable;

public class GetAttributesResponseMessage extends SerializedMessage implements Serializable {
    public final AttributesMap attributesMap;

    public GetAttributesResponseMessage(AttributesMap attributesMap) {
        this.attributesMap = attributesMap;
    }

    @Override
    public Message handle(MessageHandler m) {
        return m.handleMessage(this);
    }

}
