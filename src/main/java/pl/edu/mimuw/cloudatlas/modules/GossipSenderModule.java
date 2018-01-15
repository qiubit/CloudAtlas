package pl.edu.mimuw.cloudatlas.modules;

import com.rabbitmq.client.*;
import pl.edu.mimuw.cloudatlas.messages.*;
import pl.edu.mimuw.cloudatlas.model.ValueContact;
import pl.edu.mimuw.cloudatlas.model.ZMI;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.concurrent.ThreadLocalRandom;

public class GossipSenderModule extends Module implements MessageHandler {
    public static final String moduleID = "GossipSender";

    private final Integer LOCAL_ZMI_LEVELS;
    private final Long GOSSIP_INTERVAL = 5000L;

    private GossipStrategy strategy = GossipStrategy.GOSSIP_RANDOM;
    private Integer gossipLevelNum = null;
    private String gossipLevel = null;
    private String remoteHostname = null;
    private Connection remoteConnection = null;
    private Channel remoteChannel = null;
    private HashMap<String, ZMI> remoteZmis = null;

    public enum GossipStrategy {
        GOSSIP_RANDOM,
        GOSSIP_RR
    }

    public enum State {
        // Contacts were requested from local ZMI
        // Waiting for: gossip contacts corresponding to requested level
        // After which: gossip with random contact will be initialized
        CONTACTS_REQUESTED,
        // Remote ZMI was requested
        // Waiting for: gossip-relevant remote ZMI data
        // After which: we will request local ZMI data
        REMOTE_ZMI_REQUESTED,
        // Local ZMI was requested (for sending to remote)
        // Waiting for: gossip-relevant local ZMI data
        // After which: we will send both ZMIs to remote and local and end current gossip
        LOCAL_ZMI_REQUESTED,
    }

    public GossipSenderModule(Integer localZmiLevels) throws Exception {
        super(moduleID);
        this.LOCAL_ZMI_LEVELS = localZmiLevels;
        if (localZmiLevels <= 1) {
            throw new IllegalArgumentException("Gossip possible only with localZmiLevels >= 2");
        }
        sendMsg(moduleID, "", new InitiateGossipMessage(), Module.SERIALIZED_TYPE);
    }

    private void closeChannel(Channel channel) {
        if (channel != null) {
            try {
                // channel.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    private void closeConnection(Connection connection) {
        if (connection != null) {
            try {
                // connection.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    private void clearCurrentGossip() {
        System.out.println(moduleID + ": Clearing gossip data");
        this.gossipLevel = null;
        this.remoteHostname = null;
        closeChannel(this.remoteChannel);
        closeConnection(this.remoteConnection);
        this.remoteConnection = null;
        this.remoteChannel = null;
        this.remoteZmis = null;
    }

    @Override
    public Message handleMessage(GetZMIGossipInfoResponseMessage msg) {
        // Send remote ZMI to ZMIHandler, send local ZMI to remote host
        System.out.println(moduleID + ": Got local ZMI data [" + msg.gossippedLevel + "]");
        if (msg.gossippedLevel.equals(gossipLevel) && remoteChannel != null) {
            Message remoteRet = new GossipTransactionRemoteZMIMessage(gossipLevel, msg.relevantZMIs, msg.fallbackContacts);
            remoteRet.setSenderHostname();

            try {
                remoteChannel.queueDeclare(GossipReceiverModule.moduleID, false, false, false, null);
                AMQP.BasicProperties props = new AMQP.BasicProperties
                        .Builder()
                        .contentType(Module.SERIALIZED_TYPE)
                        .build();
                remoteChannel.basicPublish("", GossipReceiverModule.moduleID, props, remoteRet.toBytes());
                remoteChannel.close();
                System.out.println(moduleID + ": Sent local ZMI data to remote");
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        if (this.remoteZmis != null) {
            Message localRet = new GossippedZMIMessage(this.remoteZmis);
            localRet.setReceiverQueueName(ZMIHolderModule.moduleID);
            return localRet;
        }
        return null;
    }

    @Override
    public Message handleMessage(GossipTransactionRemoteZMIMessage msg) {
        // Save remote ZMI, send request for local ZMI
        System.out.println(moduleID + ": Received remote ZMI from " + msg.getSenderHostname());

        // GTP
        Long t1b = System.currentTimeMillis();

        if (msg.gossipLevel.equals(gossipLevel)) {
            remoteZmis = msg.relevantZmis;

            Message ret = new GetZMIGossipInfoRequestMessage(gossipLevel);
            ret.setReceiverQueueName(ZMIHolderModule.moduleID);
            ret.setSenderQueueName(GossipSenderModule.moduleID);

            return ret;
        }
        return null;
    }

    @Override
    public Message handleMessage(GetGossipMetadataResponseMessage msg) {
        // Contact remote ZMI

        // Irrelevant message - discard
        if (!msg.levelNum.equals(this.gossipLevelNum))
            return null;

        this.gossipLevel = msg.levelPath.toString();

        System.out.println(moduleID + ": contacts received - " + msg.contacts + " [" + this.gossipLevel + " " + this.gossipLevelNum + "]");
        ArrayList<Address> addressesList = new ArrayList<>();
        for (ValueContact contact : msg.contacts) {
            addressesList.add(new Address(contact.getAddress().getHostAddress()));
        }
        Address[] addresses = new Address[addressesList.size()];
        addresses = addressesList.toArray(addresses);

        boolean communicationFail = false;
        ConnectionFactory factory = new ConnectionFactory();
        factory.setUsername("cloudatlas");
        factory.setPassword("cloudatlas");
        try {
            Message toSend = new GossipTransactionInitMessage(this.gossipLevel);
            toSend.setSenderHostname();

            remoteConnection = factory.newConnection(addresses);
            remoteChannel = remoteConnection.createChannel();

            remoteHostname = this.remoteChannel.getConnection().getAddress().getHostAddress();
            System.out.println(moduleID + ": gossipping with " + remoteHostname);
            remoteChannel.queueDeclare(GossipReceiverModule.moduleID, false, false, false, null);

            AMQP.BasicProperties props = new AMQP.BasicProperties.Builder()
                    .replyTo(GossipSenderModule.moduleID)
                    .contentType(Module.SERIALIZED_TYPE)
                    .build();
            remoteChannel.basicPublish("", GossipReceiverModule.moduleID, props, toSend.toBytes());
        } catch (Exception e) {
            e.printStackTrace();
            communicationFail = true;
        }

        if (communicationFail) {
            System.out.println("Gossip: Gossip fail, will reinitialize automatically soon...");
        }

        return null;
    }

    private Message gossipInitiator() {
        // Note: 0 is never gossipLevel as we don't gossip the root
        Integer currentGossipLevelNum = null;
        if (strategy.equals(GossipStrategy.GOSSIP_RANDOM)) {
            currentGossipLevelNum = ThreadLocalRandom.current().nextInt(1, LOCAL_ZMI_LEVELS);
        } else if (strategy.equals(GossipStrategy.GOSSIP_RR)) {
            if (gossipLevelNum == null)
                currentGossipLevelNum = 1;
            else {
                currentGossipLevelNum += 1;
                if (currentGossipLevelNum > LOCAL_ZMI_LEVELS)
                    currentGossipLevelNum = 1;
            }
        }
        this.gossipLevelNum = currentGossipLevelNum;

        ScheduledMessage msg = new ScheduledMessage(new InitiateGossipMessage(), GOSSIP_INTERVAL);
        msg.setReceiverQueueName(moduleID);
        try {
            sendMsg(TimerModule.moduleID, null, msg, Module.SERIALIZED_TYPE, moduleID);
        } catch (Exception e) {
            e.printStackTrace();
            System.out.println(moduleID + ": FATAL ERROR - will not gossip anymore");
        }

        Message ret = new GetGossipMetadataRequestMessage(this.gossipLevelNum);
        ret.setReceiverQueueName(ZMIHolderModule.moduleID);
        return ret;
    }

    @Override
    public Message handleMessage(InitiateGossipMessage msg) {
        // Clear current gossip data, pick level, fetch contacts
        clearCurrentGossip();
        return gossipInitiator();
    }
}
