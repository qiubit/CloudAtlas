package pl.edu.mimuw.cloudatlas.agent.message;


import java.io.*;

public abstract class Message {
    private String fromModuleID;
    private String contextID;


    public abstract Message handle(MessageHandler m);

    public byte[] toBytes() throws IOException {
        ByteArrayOutputStream byte_output = new ByteArrayOutputStream();
        ObjectOutputStream output = new ObjectOutputStream(byte_output);
        output.writeObject(this);
        output.flush();
        return byte_output.toByteArray();
    }

    public static Message fromBytes(byte[] bytes) throws IOException, ClassNotFoundException {
        ByteArrayInputStream byte_input = new ByteArrayInputStream(bytes);
        ObjectInputStream input = new ObjectInputStream(byte_input);
        return (Message)input.readObject();
    }

    public String getFromModuleID() {
        return fromModuleID;
    }

    public void setFromModuleID(String fromModuleID) {
        this.fromModuleID = fromModuleID;
    }

    public String getContextID() {
        return contextID;
    }

    public void setContextID(String contextID) {
        this.contextID = contextID;
    }
}

