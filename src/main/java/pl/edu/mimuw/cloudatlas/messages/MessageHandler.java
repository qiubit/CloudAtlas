package pl.edu.mimuw.cloudatlas.messages;

public interface MessageHandler {
    Message handleMessage(Message msg);

    // Agent communication messages (Serialized)
    Message handleMessage(GetAttributesRequestMessage msg);
    Message handleMessage(GetAttributesResponseMessage msg);
    Message handleMessage(GetContactsRequestMessage msg);
    Message handleMessage(GetContactsResponseMessage msg);
    Message handleMessage(GetQueriesRequestMessage msg);
    Message handleMessage(GetQueriesResponseMessage msg);
    Message handleMessage(GetZonesRequestMessage msg);
    Message handleMessage(GetZonesResponseMessage msg);
    Message handleMessage(InstallQueryMessage msg);
    Message handleMessage(UninstallQueryMessage msg);
    Message handleMessage(SetContactsMessage msg);
    Message handleMessage(SetAttributeMessage msg);
    Message handleMessage(ExecuteQueriesMessage msg);
    Message handleMessage(GetZMILevelsRequestMessage msg);
    Message handleMessage(GetZMILevelsResponseMessage msg);
    Message handleMessage(GetZMIGossipInfoRequestMessage msg);
    Message handleMessage(GetZMIGossipInfoResponseMessage msg);
    Message handleMessage(GossippedZMIMessage msg);
    Message handleMessage(GetGossipMetadataRequestMessage msg);

    // Gossip
    Message handleMessage(InitiateGossipMessage msg);
    Message handleMessage(GossipTransactionInitMessage msg);
    Message handleMessage(GossipTransactionRemoteZMIMessage msg);
    Message handleMessage(GetGossipMetadataResponseMessage msg);

    // Timer
    Message handleMessage(ScheduledMessage msg);

    // Fetcher communication messages (JSON)
    Message handleMessage(FetcherMeasurementsMessage msg);
}
