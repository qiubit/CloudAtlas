package pl.edu.mimuw.cloudatlas.agent;

import pl.edu.mimuw.cloudatlas.model.PathName;

import java.io.Serializable;

public class GetAttributesRequestMessage extends Message implements Serializable {

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
