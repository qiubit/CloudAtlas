package pl.edu.mimuw.cloudatlas.messages;

import pl.edu.mimuw.cloudatlas.model.ValueContact;

import java.io.Serializable;
import java.util.ArrayList;

public class SetContactsMessage extends SerializedMessage implements Serializable {
    public final ArrayList<ValueContact> new_contacts;

    public SetContactsMessage(ArrayList<ValueContact> new_contacts) {
        this.new_contacts = new_contacts;
    }

    @Override
    public Message handle(MessageHandler m) {
        return m.handleMessage(this);
    }
}
