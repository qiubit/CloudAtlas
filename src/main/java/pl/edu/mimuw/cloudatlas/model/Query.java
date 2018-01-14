package pl.edu.mimuw.cloudatlas.model;

import java.io.Serializable;

public class Query implements Serializable {
    public final Attribute name;
    public final String query;

    public Query(Attribute name, String query) {
        this.name = name;
        this.query = query;
    }

    @Override
    public boolean equals(Object object) {
        if (object == null)
            return false;
        if (getClass() != object.getClass())
            return false;
        return name.equals(((Query) object).name) && query.equals(((Query) object).query);
    }

    @Override
    public int hashCode() {
        return 37 * name.hashCode() + query.hashCode();
    }
}
