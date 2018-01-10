package pl.edu.mimuw.cloudatlas.agent.message;

import java.io.Serializable;

public class WakeUpRequestMessage extends Message implements Serializable {
    public final Long wakeUpAt;

    public WakeUpRequestMessage(Long wakeUpAt) {
        this.wakeUpAt = wakeUpAt;
    }

    @Override
    public Message handle(MessageHandler m) { return m.handleMessage(this); }
}
