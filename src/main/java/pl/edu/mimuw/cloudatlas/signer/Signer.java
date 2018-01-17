package pl.edu.mimuw.cloudatlas.signer;

import pl.edu.mimuw.cloudatlas.modules.SignerModule;

import java.io.FileInputStream;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.security.KeyFactory;
import java.security.PrivateKey;
import java.security.spec.PKCS8EncodedKeySpec;
import java.util.Properties;

public class Signer {

    public static void main(String[] args) throws Exception {

        Properties properties = new Properties();
        InputStream input = new FileInputStream(args[0]);

        properties.load(input);

        Config.readFromProps(properties);
        new SignerModule(getPrivateKey(Config.getPrivateKeyPath()));
    }

    public static PrivateKey getPrivateKey(String filename) throws Exception {
        byte[] keyBytes = Files.readAllBytes(Paths.get(filename));

        PKCS8EncodedKeySpec spec =
                new PKCS8EncodedKeySpec(keyBytes);
        KeyFactory kf = KeyFactory.getInstance("RSA");
        return kf.generatePrivate(spec);
    }
}
