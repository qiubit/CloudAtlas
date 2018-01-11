package pl.edu.mimuw.cloudatlas.modules;

import com.rabbitmq.client.*;
import pl.edu.mimuw.cloudatlas.messages.*;

import java.io.*;

public abstract class Module implements MessageHandler {

    private final String moduleID;

    public static final String JSON_TYPE = "application/json";
    public static final String SERIALIZED_TYPE = "application/java-serialized-object";

    private Connection connection;
    private Channel myChannel;

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

    private Message visitMessage(Message msg) {
        return msg.handle(this);
    }

    protected Module(String moduleID) throws Exception {
        this.moduleID = moduleID;
        ConnectionFactory factory = new ConnectionFactory();
        factory.setHost("localhost");
        connection = factory.newConnection();
        myChannel = connection.createChannel();
        myChannel.queueDeclare(moduleID, false, false, false, null);
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

}
