package pl.edu.mimuw.cloudatlas.agent.module;

import pl.edu.mimuw.cloudatlas.agent.message.SetFallbackRequestMessage;
import pl.edu.mimuw.cloudatlas.agent.message.StatusResponseMessage;
import pl.edu.mimuw.cloudatlas.agent.message.GetFallbackContactsRequestMessage;
import pl.edu.mimuw.cloudatlas.agent.message.GetFallbackContactsResponseMessage;
import pl.edu.mimuw.cloudatlas.agent.message.Message;
import pl.edu.mimuw.cloudatlas.agent.message.MessageHandler;
import pl.edu.mimuw.cloudatlas.model.ValueContact;

import java.util.ArrayList;

public class FallbackContactsHolderModule extends Module implements MessageHandler {
    private static final String moduleID = "FallbackContactsHolder";

    private static ArrayList<ValueContact> fallback_contacts = new ArrayList<>();

    FallbackContactsHolderModule() throws Exception {
        super(moduleID);
    }

    @Override
    public Message handleMessage(GetFallbackContactsRequestMessage msg) {
        return new GetFallbackContactsResponseMessage((ArrayList) fallback_contacts.clone());
    }

    @Override
    public Message handleMessage(SetFallbackRequestMessage msg) {
        this.fallback_contacts = (ArrayList) msg.fallbackContacts.clone();
        return new StatusResponseMessage(true);
    }
}
