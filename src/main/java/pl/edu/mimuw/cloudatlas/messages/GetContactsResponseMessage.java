package pl.edu.mimuw.cloudatlas.messages;

import pl.edu.mimuw.cloudatlas.model.ValueContact;

import java.io.Serializable;
import java.util.List;

public class GetContactsResponseMessage extends SerializedMessage implements Serializable {
    public final List<ValueContact> contactList;

    public GetContactsResponseMessage(List<ValueContact> contactList) {
        this.contactList = contactList;
    }

    @Override
    public Message handle(MessageHandler m) {
        return m.handleMessage(this);
    }
}
