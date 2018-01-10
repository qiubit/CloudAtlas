package pl.edu.mimuw.cloudatlas.messages;

import java.io.*;

public abstract class SerializedMessage extends Message {

    @Override
    public byte[] toBytes() throws IOException {
        ByteArrayOutputStream byte_output = new ByteArrayOutputStream();
        ObjectOutputStream output = new ObjectOutputStream(byte_output);
        output.writeObject(this);
        output.flush();
        return byte_output.toByteArray();
    }

    public static SerializedMessage fromBytes(byte[] bytes) throws IOException, ClassNotFoundException {
        ByteArrayInputStream byte_input = new ByteArrayInputStream(bytes);
        ObjectInputStream input = new ObjectInputStream(byte_input);
        return (SerializedMessage) input.readObject();
    }
}
