package pl.edu.mimuw.cloudatlas.agent.message;

import java.io.Serializable;

public class GetFallbackContactsRequestMessage extends Message implements Serializable {

    @Override
    public Message handle(MessageHandler m) { return m.handleMessage(this); }
}
