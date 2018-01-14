package pl.edu.mimuw.cloudatlas.signer;

import pl.edu.mimuw.cloudatlas.modules.SignerModule;

import java.nio.file.Files;
import java.nio.file.Paths;
import java.security.KeyFactory;
import java.security.PrivateKey;
import java.security.spec.PKCS8EncodedKeySpec;

public class Signer {

    public static void main(String[] args) throws Exception {
        if (args.length != 1) {
            System.out.println("Usage: ./java <private-key-path>");
            return;
        }

        PrivateKey privateKey = getPrivateKey(args[0]);

        new SignerModule(privateKey);
    }

    private static PrivateKey getPrivateKey(String filename) throws Exception {
        byte[] keyBytes = Files.readAllBytes(Paths.get(filename));

        PKCS8EncodedKeySpec spec =
                new PKCS8EncodedKeySpec(keyBytes);
        KeyFactory kf = KeyFactory.getInstance("RSA");
        return kf.generatePrivate(spec);
    }
}
