package pl.edu.mimuw.cloudatlas.agent;

import pl.edu.mimuw.cloudatlas.model.PathName;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Properties;

public class Config {
    public static final String ZONE_NAME = "zonepath";
    public static final String LOCAL_IP = "localip";

    private static PathName zonePath = new PathName("/bruna/24/golas");
    private static String localIp = "127.0.0.1";
    private static InetAddress address = null;

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

    public static InetAddress getLocalIpInetAddr() {
        if (address == null) {
            try {
                InetAddress resolved = InetAddress.getByName(localIp);
                address = resolved;
            } catch (UnknownHostException e) {
                e.printStackTrace();
                System.out.println("WARNING Config: Submitted local IP " + localIp + " could not be resolved");
            }
        }
        return address;
    }

    @Override
    public String toString() {
        String repr = "";
        repr += ZONE_NAME + ": " + zonePath.toString();
        return repr;
    }
}
