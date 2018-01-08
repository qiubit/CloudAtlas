package pl.edu.mimuw.cloudatlas.agent;

import org.json.simple.JSONObject;

public class FetcherMeasurementsMessage extends JsonMessage {
    private final String measurementTimestamp;
    private final String measurementName;
    private final String measurementValue;

    FetcherMeasurementsMessage(JSONObject obj) {
        super(obj);

        this.measurementTimestamp = obj.get("timestamp").toString();
        this.measurementName = obj.get("name").toString();
        this.measurementValue = obj.get("value").toString();
    }

    public String getTimestamp() {
        return this.measurementTimestamp;
    }

    public String getName() {
        return this.measurementName;
    }

    public String getValue() {
        return this.measurementValue;
    }

    @Override
    public Message handle(MessageHandler m) {
        return m.handleMessage(this);
    }
}
