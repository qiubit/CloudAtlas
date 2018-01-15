package pl.edu.mimuw.cloudatlas.modules;

import com.rabbitmq.client.*;
import pl.edu.mimuw.cloudatlas.messages.*;

import java.io.*;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;

public abstract class Module implements MessageHandler {

    private final String moduleID;
    private String localIP = null;

    public static final String JSON_TYPE = "application/json";
    public static final String SERIALIZED_TYPE = "application/java-serialized-object";

    private Connection connection;
    private Channel myChannel;

    private Long id = 0L;
    protected HashMap<Long, BlockingQueue<Message>> responseQueue = new HashMap<>();
    protected final Object responseQueueLock = new Object();

    public String getLocalIP() {
        if (localIP == null) {
            try {
                localIP = InetAddress.getLocalHost().getHostAddress();
            } catch (UnknownHostException e) {
                System.out.println("Module: Could not detect local IP address");
            }
        }
        return localIP;
    }

    public String constructCorrelationId(String id, String address) {
        return id + "/" + address;
    }

    private Message visitMessage(Message msg) {
        return msg.handle(this);
    }

    @Override
    public Message handleMessage(Message msg) {
        System.out.println("Unknown message");
        return null;
    }

    @Override
    public Message handleMessage(GetFallbackContactsRequestMessage msg) {
        return handleMessage((Message) msg);
    }

    @Override
    public Message handleMessage(GetFallbackContactsResponseMessage msg) {
        return handleMessage((Message) msg);
    }

    @Override
    public Message handleMessage(GetQueriesRequestMessage msg) {
        return handleMessage((Message) msg);
    }

    @Override
    public Message handleMessage(GetQueriesResponseMessage msg) {
        return handleMessage((Message) msg);
    }

    @Override
    public Message handleMessage(GetZonesRequestMessage msg) {
        return handleMessage((Message) msg);
    }

    @Override
    public Message handleMessage(GetZonesResponseMessage msg) {
        return handleMessage((Message) msg);
    }

    @Override
    public Message handleMessage(InstallQueryMessage msg) {
        return handleMessage((Message) msg);
    }

    @Override
    public Message handleMessage(UninstallQueryMessage msg) {
        return handleMessage((Message) msg);
    }

    @Override
    public Message handleMessage(GetAttributesRequestMessage msg) {
        return handleMessage((Message) msg);
    }

    @Override
    public Message handleMessage(GetAttributesResponseMessage msg) {
        return handleMessage((Message) msg);
    }

    @Override
    public Message handleMessage(SetFallbackContactsMessage msg) {
        return handleMessage((Message) msg);
    }

    @Override
    public Message handleMessage(SetAttributeMessage msg) {
        return handleMessage((Message) msg);
    }

    @Override
    public Message handleMessage(FetcherMeasurementsMessage msg) {
        return handleMessage((Message) msg);
    }

    @Override
    public Message handleMessage(ScheduledMessage msg) {
        return handleMessage((Message) msg);
    }

    @Override
    public Message handleMessage(ExecuteQueriesMessage msg) {
        return handleMessage((Message) msg);
    }

    @Override
    public Message handleMessage(InitiateGossipMessage msg) {
        return handleMessage((Message) msg);
    }

    @Override
    public Message handleMessage(GetZMILevelsRequestMessage msg) {
        return handleMessage((Message) msg);
    }

    @Override
    public Message handleMessage(GetZMILevelsResponseMessage msg) {
        return handleMessage((Message) msg);
    }

    @Override
    public Message handleMessage(GetZMIGossipInfoRequestMessage msg) {
        return handleMessage((Message) msg);
    }

    @Override
    public Message handleMessage(GetZMIGossipInfoResponseMessage msg) {
        return handleMessage((Message) msg);
    }

    @Override
    public Message handleMessage(GossippedZMIMessage msg) {
        return handleMessage((Message) msg);
    }

    @Override
    public Message handleMessage(GossipTransactionInitMessage msg) {
        return handleMessage((Message) msg);
    }

    @Override
    public Message handleMessage(GossipTransactionRemoteZMIMessage msg) {
        return handleMessage((Message) msg);
    }

    @Override
    public Message handleMessage(GetGossipMetadataRequestMessage msg) {
        return handleMessage((Message) msg);
    }

    @Override
    public Message handleMessage(GetGossipMetadataResponseMessage msg) {
        return handleMessage((Message) msg);
    }

    private class DeliveryHandler {
        private final String hostname;

        public DeliveryHandler(String hostname) {
            this.hostname = hostname;
        }

        public void handleDelivery(String consumerTag, Envelope envelope,
                                   AMQP.BasicProperties properties, byte[] body) {

        }
    }

    protected Module(String moduleID) throws Exception {
        System.out.println(moduleID + ": starting");
        this.moduleID = moduleID;
        ConnectionFactory factory = new ConnectionFactory();
        factory.setHost("localhost");
        factory.setUsername("cloudatlas");
        factory.setPassword("cloudatlas");
        connection = factory.newConnection();
        myChannel = connection.createChannel();
        myChannel.queueDeclare(moduleID, false, false, false, null);

        // Fetch references
        Object responseQueueLock = this.responseQueueLock;
        HashMap<Long, BlockingQueue<Message>> responseQueue = this.responseQueue;

        Consumer consumer = new DefaultConsumer(myChannel) {
            @Override
            public void handleDelivery(String consumerTag, Envelope envelope,
                                       AMQP.BasicProperties properties, byte[] body) throws IOException {
                try {
                    Message message;
                    String contentType = properties.getContentType();

                    if (contentType == null) {
                        throw new IOException("Message without content_type property received");
                    }

                    if (contentType.equals(JSON_TYPE)) {
                        message = JsonMessage.fromBytes(body);
                    } else if (contentType.equals(SERIALIZED_TYPE)) {
                        message = SerializedMessage.fromBytes(body);
                    } else {
                        throw new IOException("Unexpected msg contentType: " + contentType);
                    }

                    // If some thread is waiting for correlated message, it will handle it
                    synchronized (responseQueueLock) {
                        Long correlationId = null;
                        try {
                            correlationId = Long.parseLong(properties.getCorrelationId());
                        } catch (NumberFormatException e) {
                            correlationId = null;
                        }
                        if (responseQueue.get(correlationId) != null) {
                            try {
                                responseQueue.get(correlationId).put(message);
                            } catch (InterruptedException e) {
                                System.out.println("Module: Could not put into responseQueue");
                                e.printStackTrace();
                            }
                            return;
                        }
                    }

                    Message response = visitMessage(message);
                    // TODO: Allow JSON response?
                    if (response != null) {
                        AMQP.BasicProperties replyProps = new AMQP.BasicProperties
                                .Builder()
                                .correlationId(properties.getCorrelationId())
                                .contentType(Module.SERIALIZED_TYPE)
                                .build();

                        String replyToQueue;
                        String replyToHostname;
                        if (response.getReceiverQueueName() != null)
                            replyToQueue = response.getReceiverQueueName();
                        else
                            replyToQueue = properties.getReplyTo();

                        if (response.getReceiverHostname() != null)
                            replyToHostname = response.getReceiverHostname();
                        else
                            replyToHostname = "localhost";

                        // We can use channel for localhost contact
                        if (replyToHostname.equals("localhost")
                                || replyToHostname.equals(InetAddress.getLocalHost().getHostAddress()))
                            myChannel.basicPublish("", replyToQueue, replyProps, response.toBytes());
                        // We must create new, temporary channel and connection
                        else {
                            try {
                                ConnectionFactory factory;
                                Connection connection;
                                Channel channel;

                                System.out.println("Module: Sending message to " + replyToHostname);

                                factory = new ConnectionFactory();
                                factory.setHost(replyToHostname);
                                factory.setUsername("cloudatlas");
                                factory.setPassword("cloudatlas");

                                connection = factory.newConnection();
                                channel = connection.createChannel();
                                channel.queueDeclare(replyToQueue, false, false, false, null);

                                channel.basicPublish("", replyToQueue, replyProps, response.toBytes());
                                channel.close();
                                connection.close();
                            } catch (Exception e) {
                                e.printStackTrace();
                                System.out.println("Module: Couldn't send remote message");
                            }
                        }
                    }
                } catch (ClassNotFoundException e) {
                    System.out.println("Got Invalid msg");
                }
            }
        };
        myChannel.basicConsume(moduleID, true, consumer);
    }

    protected void sendMsg(String recieverModuleID, String msgID, Message msg, String msgContentType)
            throws java.io.IOException {
        AMQP.BasicProperties props = new AMQP.BasicProperties
                .Builder()
                .correlationId(msgID)
                .replyTo(recieverModuleID)
                .contentType(msgContentType)
                .build();
        myChannel.basicPublish("", recieverModuleID, props, msg.toBytes());
    }

    protected void sendMsg(String recieverModuleID, String msgID, Message msg, String msgContentType, String senderModuleId)
            throws java.io.IOException {
        AMQP.BasicProperties props = new AMQP.BasicProperties
                .Builder()
                .correlationId(msgID)
                .replyTo(senderModuleId)
                .contentType(msgContentType)
                .build();
        myChannel.basicPublish("", recieverModuleID, props, msg.toBytes());
    }

    protected Long getFreeId() {
        Long freeId = this.id++;
        synchronized (this.responseQueueLock) {
            this.responseQueue.put(freeId, new ArrayBlockingQueue<Message>(1));
        }
        return freeId;
    }
}
