package pl.edu.mimuw.cloudatlas.modules;

import com.rabbitmq.client.*;
import pl.edu.mimuw.cloudatlas.messages.*;

import java.io.*;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;

public abstract class Module implements MessageHandler {

    private final String moduleID;

    public static final String JSON_TYPE = "application/json";
    public static final String SERIALIZED_TYPE = "application/java-serialized-object";

    private Connection connection;
    private Channel myChannel;

    private Long id = 0L;
    protected HashMap<Long, BlockingQueue<Message>> responseQueue = new HashMap<>();
    protected final Object responseQueueLock = new Object();

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

    protected Module(String moduleID) throws Exception {
        System.out.println(moduleID + ": starting");
        this.moduleID = moduleID;
        ConnectionFactory factory = new ConnectionFactory();
        factory.setHost("localhost");
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

                        String replyTo;
                        if (response.getReceiverQueueName() != null)
                            replyTo = response.getReceiverQueueName();
                        else
                            replyTo = properties.getReplyTo();
                        myChannel.basicPublish("", replyTo, replyProps, response.toBytes());
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
