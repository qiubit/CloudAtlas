package pl.edu.mimuw.cloudatlas.messages;

public interface MessageHandler {
    Message handleMessage(Message msg);

    // Agent communication messages (Serialized)
    Message handleMessage(GetAttributesRequestMessage msg);
    Message handleMessage(GetAttributesResponseMessage msg);
    Message handleMessage(GetFallbackContactsRequestMessage msg);
    Message handleMessage(GetFallbackContactsResponseMessage msg);
    Message handleMessage(GetQueriesRequestMessage msg);
    Message handleMessage(GetQueriesResponseMessage msg);
    Message handleMessage(GetZonesRequestMessage msg);
    Message handleMessage(GetZonesResponseMessage msg);
    Message handleMessage(InstallQueryMessage msg);
    Message handleMessage(UninstallQueryMessage msg);
    Message handleMessage(SetFallbackContactsMessage msg);
    Message handleMessage(SetAttributeMessage msg);
    Message handleMessage(ExecuteQueriesMessage msg);
    Message handleMessage(GetZMILevelsRequestMessage msg);
    Message handleMessage(GetZMILevelsResponseMessage msg);
    Message handleMessage(GetZMIGossipInfoRequestMessage msg);
    Message handleMessage(GetZMIGossipInfoResponseMessage msg);
    Message handleMessage(GossippedZMIMessage msg);

    // Gossip
    Message handleMessage(InitiateGossipMessage msg);

    // Timer
    Message handleMessage(ScheduledMessage msg);

    // Fetcher communication messages (JSON)
    Message handleMessage(FetcherMeasurementsMessage msg);
}
