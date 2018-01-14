package pl.edu.mimuw.cloudatlas.agent;

import pl.edu.mimuw.cloudatlas.model.PathName;

import java.util.Properties;

public class Config {
    public static final String ZONE_NAME = "zonepath";

    private PathName zonePath;

    public Config(Properties prop) {
        System.out.println(prop.getProperty(ZONE_NAME));
        zonePath = new PathName(prop.getProperty(ZONE_NAME));
    }

    public PathName getZonePath() {
        return this.zonePath;
    }

    @Override
    public String toString() {
        String repr = "";
        repr += ZONE_NAME + ": " + zonePath.toString();
        return repr;
    }
}
