package pl.edu.mimuw.cloudatlas.agent;

import org.json.simple.JSONObject;
import pl.edu.mimuw.cloudatlas.model.Type;
import pl.edu.mimuw.cloudatlas.model.TypePrimitive;

import java.util.HashMap;
import java.util.Map;

public class FetcherMeasurementsMessage extends JsonMessage {
    private final String measurementTimestamp;
    private final String measurementName;
    private final String measurementValue;

    private static final Map<String, Type> measurementToType;

    static {
        measurementToType = new HashMap<>();
        measurementToType.put("total_swap", TypePrimitive.DOUBLE);
        measurementToType.put("total_disk", TypePrimitive.DOUBLE);
        measurementToType.put("logged_users", TypePrimitive.DOUBLE);
        measurementToType.put("total_ram", TypePrimitive.DOUBLE);
        measurementToType.put("free_disk", TypePrimitive.DOUBLE);
        measurementToType.put("num_processes", TypePrimitive.DOUBLE);
        measurementToType.put("free_swap", TypePrimitive.DOUBLE);
        measurementToType.put("cpu_load", TypePrimitive.DOUBLE);
        measurementToType.put("free_ram", TypePrimitive.DOUBLE);

        measurementToType.put("total_swap_avg", TypePrimitive.DOUBLE);
        measurementToType.put("total_disk_avg", TypePrimitive.DOUBLE);
        measurementToType.put("logged_users_avg", TypePrimitive.DOUBLE);
        measurementToType.put("total_ram_avg", TypePrimitive.DOUBLE);
        measurementToType.put("free_disk_avg", TypePrimitive.DOUBLE);
        measurementToType.put("num_processes_avg", TypePrimitive.DOUBLE);
        measurementToType.put("free_swap_avg", TypePrimitive.DOUBLE);
        measurementToType.put("cpu_load_avg", TypePrimitive.DOUBLE);
        measurementToType.put("free_ram_avg", TypePrimitive.DOUBLE);
    }

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

    public Type getMeasurementType() {
        if (measurementToType.get(this.measurementName) != null)
            return measurementToType.get(this.measurementName);
        else
            return TypePrimitive.STRING;
    }

    @Override
    public Message handle(MessageHandler m) {
        return m.handleMessage(this);
    }
}
