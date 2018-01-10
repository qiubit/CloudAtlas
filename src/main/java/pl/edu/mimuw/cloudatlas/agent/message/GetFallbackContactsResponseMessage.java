package pl.edu.mimuw.cloudatlas.agent.message;

import pl.edu.mimuw.cloudatlas.model.ValueContact;

import java.io.Serializable;
import java.util.ArrayList;

public class GetFallbackContactsResponseMessage extends Message implements Serializable {
    public final ArrayList<ValueContact> fallbackContacts;

    public GetFallbackContactsResponseMessage(ArrayList<ValueContact> fallbackContacts) {
        this.fallbackContacts = fallbackContacts;
    }

    @Override
    public Message handle(MessageHandler m) { return m.handleMessage(this); }

}
