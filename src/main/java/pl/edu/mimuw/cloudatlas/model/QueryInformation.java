package pl.edu.mimuw.cloudatlas.model;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class QueryInformation implements Serializable {
    private String query;
    private ArrayList<Attribute> attributes;
    private Long timestamp;

    public QueryInformation(String query, ArrayList<Attribute> attributes) {
        this.query = query;
        this.attributes = attributes;
        this.timestamp = System.currentTimeMillis();
    }

    public String getQuery() {
        return query;
    }

    public List<Attribute> getAttributes() {
        return attributes;
    }

    public void updateTimestamp() {
        this.timestamp = System.currentTimeMillis();
    }

    public Long getTimestamp() {
        return this.timestamp;
    }
}
