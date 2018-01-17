package pl.edu.mimuw.cloudatlas.agent;

import pl.edu.mimuw.cloudatlas.model.PathName;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Properties;

public class Config {
    public static final String ZONE_NAME = "zonepath";
    public static final String LOCAL_IP = "localip";
    public static final String PUBLIC_KEY_PATH = "publickey";
    public static final String SIGNER_IP = "signerip";

    private static PathName zonePath = new PathName("/bruna/24/golas");
    private static String localIp = "127.0.0.1";
    private static InetAddress address = null;
    private static String publicKeyPath = "";
    private static String signerIp = "localhost";

    public static void readFromProps(Properties prop) {
        System.out.println(prop.getProperty(ZONE_NAME));
        zonePath = new PathName(prop.getProperty(ZONE_NAME));
        localIp = prop.getProperty(LOCAL_IP);
        publicKeyPath = prop.getProperty(PUBLIC_KEY_PATH);
        signerIp = prop.getProperty(SIGNER_IP);
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

    public static String getPublicKeyPath() { return publicKeyPath; }

    public static String getSignerIp() {
        return signerIp;
    }

    @Override
    public String toString() {
        String repr = "";
        repr += ZONE_NAME + ": " + zonePath.toString() + "\n";
        repr += "Signer: " + signerIp + " Public key: " + publicKeyPath;
        return repr;
    }
}
