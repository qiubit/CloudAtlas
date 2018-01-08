package pl.edu.mimuw.cloudatlas.agent;


import java.io.*;

public abstract class Message {
    public abstract Message handle(MessageHandler m);
    public abstract byte[] toBytes() throws IOException;
}
