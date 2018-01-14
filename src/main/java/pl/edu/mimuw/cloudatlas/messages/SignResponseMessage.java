package pl.edu.mimuw.cloudatlas.messages;

import java.io.Serializable;

public class SignResponseMessage extends SerializedMessage implements Serializable {
    public final byte[] signature;

    public SignResponseMessage(byte[] signature) {
        this.signature = signature;
    }

    @Override
    public Message handle(MessageHandler m) {
        return m.handleMessage(this);
    }
}
