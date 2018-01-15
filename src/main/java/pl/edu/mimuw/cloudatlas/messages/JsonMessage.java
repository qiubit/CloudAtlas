package pl.edu.mimuw.cloudatlas.messages;


import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import java.io.IOException;

public class JsonMessage extends Message {
    private JSONObject obj;

    JsonMessage() {
        this.obj = null;
    }

    JsonMessage(JSONObject obj) {
        this.obj = obj;
    }

    @Override
    public byte[] toBytes() {
        return obj.toJSONString().getBytes();
    }

    @Override
    public Message handle(MessageHandler m) {
        System.out.println("JSON arrived:");
        System.out.println(obj.toJSONString());
        System.out.println();
        return m.handleMessage(this);
    }

    public JSONObject getKey(String key) {
        return (JSONObject) this.obj.get(key);
    }

    public static JsonMessage fromBytes(byte[] bytes) throws IOException {
        JSONObject obj;
        JSONParser parser = new JSONParser();
        try {
            obj = (JSONObject) parser.parse(new String(bytes));
        } catch (ParseException e) {
            throw new IOException("Could not parse JSON");
        }

        if (obj.get("topic").equals("measurements")) {
            return new FetcherMeasurementsMessage(obj);
        } else {
            return new JsonMessage(obj);
        }
    }
}
