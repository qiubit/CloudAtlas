package pl.edu.mimuw.cloudatlas.signer;

import pl.edu.mimuw.cloudatlas.model.PathName;

import java.util.Properties;

public class Config {

    public static final String PRIVATE_KEY_PATH = "privatekey";

    private static String privateKeyPath = "";

    public static void readFromProps(Properties prop) {
        privateKeyPath = prop.getProperty(PRIVATE_KEY_PATH);
    }

    public static String getPrivateKeyPath() { return privateKeyPath; }

    @Override
    public String toString() {
        String repr = "";
        repr += "Private key: " + privateKeyPath;
        return repr;
    }
}
