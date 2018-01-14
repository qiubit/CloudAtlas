package pl.edu.mimuw.cloudatlas.messages;


import java.io.*;

public abstract class Message implements Serializable {
    public String receiverQueueName;
    private boolean error = false;

    public abstract Message handle(MessageHandler m);
    public abstract byte[] toBytes() throws IOException;

    public void setReceiverQueueName(String receiverQueueName) { this.receiverQueueName = receiverQueueName; }
    public String getReceiverQueueName() { return this.receiverQueueName; }
    public void setError() { error = true; }
    public boolean isError() { return error; }
}
