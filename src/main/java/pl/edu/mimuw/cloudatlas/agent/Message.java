package pl.edu.mimuw.cloudatlas.agent;


import java.io.*;

public abstract class Message {
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
}

