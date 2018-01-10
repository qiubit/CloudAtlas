package pl.edu.mimuw.cloudatlas.messages;

import pl.edu.mimuw.cloudatlas.model.PathName;

import java.io.Serializable;

public class GetAttributesRequestMessage extends SerializedMessage implements Serializable {

    public final PathName zonePath;

    public GetAttributesRequestMessage(PathName zonePath) {
        this.zonePath = zonePath;
    }

    @Override
    public Message handle(MessageHandler m) {
        System.out.println("siemanko");
        return m.handleMessage(this);
    }
}
