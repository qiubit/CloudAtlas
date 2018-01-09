package pl.edu.mimuw.cloudatlas.agent;

public interface MessageHandler {
    Message handleMessage(Message msg);
    Message handleMessage(GetAttributesRequestMessage msg);
    Message handleMessage(GetAttributesResponseMessage msg);
    Message handleMessage(FetcherMeasurementsMessage msg);
    Message handleMessage(SetAttributeMessage msg);
}
