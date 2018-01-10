package pl.edu.mimuw.cloudatlas.agent.message;

import pl.edu.mimuw.cloudatlas.model.PathName;

import java.io.Serializable;

public class GetAttributesRequestMessage extends Message implements Serializable {

    public final PathName zonePath;

    public GetAttributesRequestMessage(PathName zonePath) {
        this.zonePath = zonePath;
    }

    @Override
    public Message handle(MessageHandler m) {
        return m.handleMessage(this);
    }
}
