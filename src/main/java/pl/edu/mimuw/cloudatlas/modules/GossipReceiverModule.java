package pl.edu.mimuw.cloudatlas.modules;

import com.rabbitmq.client.AMQP;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import pl.edu.mimuw.cloudatlas.messages.*;
import pl.edu.mimuw.cloudatlas.modules.utils.GossipTransaction;

import java.util.HashMap;
import java.util.HashSet;

public class GossipReceiverModule extends Module implements MessageHandler {
    public static final String moduleID = "GossipReceiver";
    private HashMap<String, GossipTransaction> hostnameToTransaction = new HashMap<>();
    private HashMap<String, HashSet<String>> gossipLevelToHostnames = new HashMap<>();
    private HashMap<String, Connection> hostnameToRabbitMqConnection = new HashMap<>();

    public GossipReceiverModule() throws Exception {
        super(moduleID);
    }

    private void finalizeTransaction(GossipTransaction transaction) {
        System.out.println(
                moduleID + ": Finalizing transaction with " + transaction.getInitiatorHostname()
                        + " [" + transaction.getGossipLevel() + "]"
        );
        this.hostnameToTransaction.remove(transaction.getInitiatorHostname());
        this.gossipLevelToHostnames.get(transaction.getGossipLevel()).remove(transaction.getInitiatorHostname());
        try {
            this.hostnameToRabbitMqConnection.get(transaction.getInitiatorHostname()).close();
        } catch (Exception e) {
            e.printStackTrace();
        }
        this.hostnameToRabbitMqConnection.remove(transaction.getInitiatorHostname());
    }

    @Override
    public Message handleMessage(GossipTransactionRemoteZMIMessage msg) {
        // Remote ZMI arrived - finish up relevant GossipTransactions and send it to ZMIHolder
        String sender = msg.getSenderHostname();
        GossipTransaction transaction = this.hostnameToTransaction.get(sender);
        System.out.println(moduleID + ": Got msg from remote " + sender + " - " + msg.gossipLevel);
        if (transaction != null
                && transaction.getState().equals(GossipTransaction.State.LOCAL_ZMI_SENT)
                && transaction.getGossipLevel().equals(msg.gossipLevel)) {
            finalizeTransaction(transaction);
            Message toSend = new GossippedZMIMessage(msg.relevantZmis, msg.queries);
            toSend.setReceiverQueueName(ZMIHolderModule.moduleID);
            return toSend;
        } else {
            System.out.println(
                    moduleID + ": discarding message [from: " + sender + " level: " + msg.gossipLevel + "]"
            );
        }
        return null;
    }

    @Override
    public Message handleMessage(GetZMIGossipInfoResponseMessage msg) {
        // Local ZMI arrived - update all pending GossipTransactions and send them local ZMI data
        if (gossipLevelToHostnames.get(msg.gossippedLevel) != null) {
            System.out.println(moduleID + ": Got local info for " + msg.gossippedLevel);
            for (String hostname : gossipLevelToHostnames.get(msg.gossippedLevel)) {
                Connection conn = hostnameToRabbitMqConnection.get(hostname);
                GossipTransaction transaction = hostnameToTransaction.get(hostname);
                if (transaction == null)
                    continue;
                if (!transaction.getState().equals(GossipTransaction.State.REQUESTED))
                    continue;
                if (conn != null) {
                    Message msgRemote =
                            new GossipTransactionRemoteZMIMessage(
                                    msg.gossippedLevel,
                                    msg.relevantZMIs,
                                    msg.fallbackContacts,
                                    msg.queries
                            );
                    msgRemote.setSenderHostname();
                    System.out.println(moduleID + ": Trying to send local " + msg.gossippedLevel + " info to " + hostname);

                    Channel channel;
                    try {
                        channel = conn.createChannel();
                        channel.queueDeclare(GossipSenderModule.moduleID, false, false, false, null);
                        AMQP.BasicProperties props = new AMQP.BasicProperties.Builder()
                                .replyTo(GossipReceiverModule.moduleID)
                                .contentType(Module.SERIALIZED_TYPE)
                                .build();
                        channel.basicPublish("", GossipSenderModule.moduleID, props, msgRemote.toBytes());
                        System.out.println(moduleID + ": Sending successful");
                        // channel.close();
                        transaction.markLocalZmiSent();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }
        }
        return null;
    }

    @Override
    public Message handleMessage(GossipTransactionInitMessage msg) {
        // Create connection with other agent, and send request for relevant local ZMI

        // T2A timestamp (GTP)
        Long msgRecvTs = System.currentTimeMillis();

        System.out.println(moduleID + ": GossipTransaction " + msg.gossipLevel + " [" + msg.getSenderHostname() + "]");

        if (msg.getSenderHostname() == null) {
            System.out.println(moduleID + ": No sender in GossipTransactionInitMessage. Discarding.");
        }
        String sender = msg.getSenderHostname();
        String gossipLevel = msg.gossipLevel;

        // Create connection to hostname
        ConnectionFactory factory = new ConnectionFactory();
        factory.setUsername("cloudatlas");
        factory.setPassword("cloudatlas");
        factory.setHost(sender);
        Connection connection = null;
        try {
            connection = factory.newConnection();
        } catch (Exception e) {
            e.printStackTrace();
        }
        // Discard transaction request if cannot communicate
        if (connection == null)
            return null;

        this.hostnameToRabbitMqConnection.put(sender, connection);
        this.hostnameToTransaction.put(sender, new GossipTransaction(sender, gossipLevel));
        this.gossipLevelToHostnames.computeIfAbsent(gossipLevel, k -> new HashSet<>()).add(sender);

        Message ret = new GetZMIGossipInfoRequestMessage(gossipLevel);
        ret.setReceiverQueueName(ZMIHolderModule.moduleID);
        ret.setSenderQueueName(GossipReceiverModule.moduleID);

        return ret;
    }
}
