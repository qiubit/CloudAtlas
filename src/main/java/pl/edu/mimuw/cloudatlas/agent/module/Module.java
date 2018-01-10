package pl.edu.mimuw.cloudatlas.agent.module;

import com.rabbitmq.client.*;
import pl.edu.mimuw.cloudatlas.agent.message.SetFallbackRequestMessage;
import pl.edu.mimuw.cloudatlas.agent.message.StatusResponseMessage;
import pl.edu.mimuw.cloudatlas.agent.message.*;

import java.io.*;

public abstract class Module implements MessageHandler {

    private final String moduleID;

    private Connection connection;

    // Recieveing and sending can be executed by different threads
    private Channel recieveChannel, sendChannel;

    @Override
    public Message handleMessage(Message msg) {
        System.out.println("Unknown message");
        return null;
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
    public Message handleMessage(GetAvailableZonesRequestMessage msg) {
        return handleMessage((Message) msg);
    }
    @Override
    public Message handleMessage(GetAvailableZonesResponseMessage msg) {
        return handleMessage((Message) msg);
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
    public Message handleMessage(SetFallbackRequestMessage msg) {
        return handleMessage((Message) msg);
    }
    @Override
    public Message handleMessage(StatusResponseMessage msg) {
        return handleMessage((Message) msg);
    }
    @Override
    public Message handleMessage(WakeUpRequestMessage msg) {
        return handleMessage((Message) msg);
    }
    @Override
    public Message handleMessage(WakeUpResponseMessage msg) {
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
        recieveChannel = connection.createChannel();
        recieveChannel.queueDeclare(moduleID, false, false, false, null);
        sendChannel = connection.createChannel();
        sendChannel.queueDeclare(moduleID, false, false, false, null);
        Consumer consumer = new DefaultConsumer(recieveChannel) {
            @Override
            public void handleDelivery(String consumerTag, Envelope envelope,
                                       AMQP.BasicProperties properties, byte[] body) throws IOException {
                try {
                    Message message = Message.fromBytes(body);
                    message.setContextID(properties.getCorrelationId());
                    message.setFromModuleID(properties.getReplyTo());
                    Message response = visitMessage(message);
                    if (response != null) {
                        AMQP.BasicProperties replyProps = new AMQP.BasicProperties
                                .Builder()
                                .correlationId(properties.getCorrelationId())
                                .build();

                        recieveChannel.basicPublish("", properties.getReplyTo(), replyProps, response.toBytes());
                        System.out.println("Sending reply");
                    }
                } catch (ClassNotFoundException e) {
                    System.out.println("Got Invalid msg");
                }
            }
        };
        recieveChannel.basicConsume(moduleID, true, consumer);
    }

    protected void sendMsg(String recieverModuleID, String msgID, Message msg) throws java.io.IOException {
        AMQP.BasicProperties props = new AMQP.BasicProperties
                .Builder()
                .correlationId(msgID)
                .replyTo(moduleID)
                .build();
        sendChannel.basicPublish("", recieverModuleID, props, msg.toBytes());
    }

}
