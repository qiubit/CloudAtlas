package pl.edu.mimuw.cloudatlas.agent;

import pl.edu.mimuw.cloudatlas.model.PathName;

import java.util.Properties;

public class Config {
    public static final String ZONE_NAME = "zonepath";
    public static final String LOCAL_IP = "localip";

    private static PathName zonePath = new PathName("/bruna/24/golas");
    private static String localIp = "localhost";

    public static void readFromProps(Properties prop) {
        System.out.println(prop.getProperty(ZONE_NAME));
        zonePath = new PathName(prop.getProperty(ZONE_NAME));
        localIp = prop.getProperty(LOCAL_IP);
    }

    public static PathName getZonePath() {
        return zonePath;
    }

    public static String getLocalIp() {
        return localIp;
    }

    @Override
    public String toString() {
        String repr = "";
        repr += ZONE_NAME + ": " + zonePath.toString();
        return repr;
    }
}
