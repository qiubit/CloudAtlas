package pl.edu.mimuw.cloudatlas.messages;

import java.io.Serializable;

public class GetZonesResponseMessage extends SerializedMessage implements Serializable {
    @Override
    public Message handle(MessageHandler m) {
        return m.handleMessage(this);
    }
}
