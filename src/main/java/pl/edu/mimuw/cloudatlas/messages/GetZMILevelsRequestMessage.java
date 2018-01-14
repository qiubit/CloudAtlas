package pl.edu.mimuw.cloudatlas.messages;


import java.io.Serializable;

public class GetZMILevelsRequestMessage extends SerializedMessage implements Serializable {
    @Override
    public Message handle(MessageHandler m) {
        return m.handleMessage(this);
    }
}
