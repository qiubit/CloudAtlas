package pl.edu.mimuw.cloudatlas.messages;


import java.io.*;
import java.net.InetAddress;

public abstract class Message implements Serializable {
    private String senderHostname = null;
    private String receiverQueueName;
    private String receiverHostname = "localhost";

    public abstract Message handle(MessageHandler m);
    public abstract byte[] toBytes() throws IOException;

    public void setReceiverQueueName(String receiverQueueName) { this.receiverQueueName = receiverQueueName; }
    public String getReceiverQueueName() { return this.receiverQueueName; }

    public void setSenderHostname() {
        try {
            this.senderHostname = InetAddress.getLocalHost().getHostAddress();
        } catch (Exception e) {
            e.printStackTrace();
            System.out.println("Message: senderHostname could not be set");
        }
    }

    public String getSenderHostname() {
        return this.senderHostname;
    }

    public void setReceiverHostname(String receiverHostname) {
        this.receiverHostname = receiverHostname;
    }

    public String getReceiverHostname() {
        return this.receiverHostname;
    }
}
