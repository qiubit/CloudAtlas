package pl.edu.mimuw.cloudatlas.messages;


import java.io.*;
import java.net.InetAddress;

public abstract class Message implements Serializable {
    private String senderQueueName = null;
    private String senderHostname = null;
    private String receiverQueueName = null;
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
        String localIP = "localhost";
        try {
            localIP = InetAddress.getLocalHost().getHostAddress();
        } catch (Exception e) {
            e.printStackTrace();
        }
        if (senderHostname != null) {
            if (senderHostname.equals(localIP)
                    || senderHostname.equals("localhost")
                    || senderHostname.equals("127.0.0.1"))
                return "localhost";
        }
        return this.senderHostname;
    }

    public void setReceiverHostname(String receiverHostname) {
        this.receiverHostname = receiverHostname;
    }

    public String getReceiverHostname() {
        return this.receiverHostname;
    }

    public void setSenderQueueName(String senderQueueName) {
        this.senderQueueName = senderQueueName;
    }

    public String getSenderQueueName() {
        return this.senderQueueName;
    }
}
