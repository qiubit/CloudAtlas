package pl.edu.mimuw.cloudatlas.agent.message;


import java.io.Serializable;

public class StatusResponseMessage extends Message implements Serializable {
    public final boolean status;

    public StatusResponseMessage(boolean status) {
        this.status = status;
    }

    @Override
    public Message handle(MessageHandler m) { return m.handleMessage(this); }
}
