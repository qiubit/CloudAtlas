package pl.edu.mimuw.cloudatlas.messages;

import pl.edu.mimuw.cloudatlas.model.PathName;

import java.io.Serializable;
import java.util.List;

public class GetZonesResponseMessage extends SerializedMessage implements Serializable {
    public final List<PathName> zones;

    public GetZonesResponseMessage(List<PathName> zones) {
        this.zones = zones;
    }

    @Override
    public Message handle(MessageHandler m) {
        return m.handleMessage(this);
    }
}
