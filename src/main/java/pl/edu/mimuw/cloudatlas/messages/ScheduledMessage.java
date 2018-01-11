package pl.edu.mimuw.cloudatlas.messages;

import java.io.Serializable;

public class ScheduledMessage extends SerializedMessage implements Serializable {
    public final Message msg;
    public final long delayInMillis;

    public ScheduledMessage(Message msg, long delayInMillis) {
        this.msg = msg;
        this.delayInMillis = delayInMillis;
    }

    @Override
    public Message handle(MessageHandler m) {
        return m.handleMessage(this);
    }
}
