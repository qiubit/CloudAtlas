package pl.edu.mimuw.cloudatlas.agent;

import com.rabbitmq.client.*;

import java.io.*;

public abstract class Module implements MessageHandler {

    private final String moduleID;

    private Connection connection;
    private Channel myChannel;

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
                    Message message = Message.fromBytes(body);
                    Message response = visitMessage(message);
                    if (response != null) {
                        AMQP.BasicProperties replyProps = new AMQP.BasicProperties
                                .Builder()
                                .correlationId(properties.getCorrelationId())
                                .build();

                        myChannel.basicPublish("", properties.getReplyTo(), replyProps, response.toBytes());
                    }
                } catch (ClassNotFoundException e) {
                    System.out.println("Got Invalid msg");
                }
            }
        };
        myChannel.basicConsume(moduleID, true, consumer);
    }

    protected void sendMsg(String recieverModuleID, String msgID, Message msg) throws java.io.IOException {
        AMQP.BasicProperties props = new AMQP.BasicProperties
                .Builder()
                .correlationId(msgID)
                .replyTo(moduleID)
                .build();
        myChannel.basicPublish("", recieverModuleID, props, msg.toBytes());
    }

}
