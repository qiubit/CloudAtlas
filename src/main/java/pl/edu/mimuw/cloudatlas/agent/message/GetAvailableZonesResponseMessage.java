package pl.edu.mimuw.cloudatlas.agent.message;

import pl.edu.mimuw.cloudatlas.model.PathName;

import java.io.Serializable;
import java.util.ArrayList;

public class GetAvailableZonesResponseMessage extends Message implements Serializable {
    public final ArrayList<PathName> availableZones;

    public GetAvailableZonesResponseMessage(ArrayList<PathName> availableZones) {
        this.availableZones = availableZones;
    }

    @Override
    public Message handle(MessageHandler m) { return m.handleMessage(this); }
}
