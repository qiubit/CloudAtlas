package pl.edu.mimuw.cloudatlas.agent;


import java.io.*;

public abstract class Message {
    protected String receiverQueueName = null;

    public abstract Message handle(MessageHandler m);
    public abstract byte[] toBytes() throws IOException;

    public String getReceiverQueueName() { return this.receiverQueueName; };
}
