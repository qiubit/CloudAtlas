package pl.edu.mimuw.cloudatlas.model;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class QueryInformation implements Serializable {
    private String query;
    private ArrayList<Attribute> attributes;

    public QueryInformation(String query, ArrayList<Attribute> attributes) {
        this.query = query;
        this.attributes = attributes;
    }

    public String getQuery() {
        return query;
    }

    public List<Attribute> getAttributes() {
        return attributes;
    }
}
