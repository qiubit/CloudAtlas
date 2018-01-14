package pl.edu.mimuw.cloudatlas.messages;

import java.io.Serializable;
import java.util.HashSet;

public class GetZMILevelsResponseMessage extends SerializedMessage implements Serializable {
    public final HashSet<String> levels;

    public GetZMILevelsResponseMessage(HashSet<String> levels) {
        this.levels = levels;
    }

    @Override
    public Message handle(MessageHandler m) {
        return m.handleMessage(this);
    }
}
