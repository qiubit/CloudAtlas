package pl.edu.mimuw.cloudatlas.agent.message;

public interface MessageHandler {
    Message handleMessage(Message msg);
    Message handleMessage(GetAttributesRequestMessage msg);
    Message handleMessage(GetAttributesResponseMessage msg);
    Message handleMessage(GetAvailableZonesRequestMessage msg);
    Message handleMessage(GetAvailableZonesResponseMessage msg);
    Message handleMessage(GetFallbackContactsRequestMessage msg);
    Message handleMessage(GetFallbackContactsResponseMessage msg);
    Message handleMessage(SetFallbackRequestMessage msg);
    Message handleMessage(StatusResponseMessage msg);
    Message handleMessage(WakeUpRequestMessage msg);
    Message handleMessage(WakeUpResponseMessage msg);
}
