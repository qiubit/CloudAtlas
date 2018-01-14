package pl.edu.mimuw.cloudatlas.signer;

import pl.edu.mimuw.cloudatlas.modules.SignerModule;

import java.nio.file.Files;
import java.nio.file.Paths;
import java.security.KeyFactory;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;

public class Signer {

    public static void main(String[] args) throws Exception {
        if (args.length != 2) {
            System.out.println("Usage: ./java <public-key-path> <private-key-path>");
            return;
        }

        PublicKey publicKey = getPublicKey(args[0]);
        PrivateKey privateKey = getPrivateKey(args[1]);

        new SignerModule(privateKey ,publicKey);
    }

    private static PrivateKey getPrivateKey(String filename) throws Exception {
        byte[] keyBytes = Files.readAllBytes(Paths.get(filename));

        PKCS8EncodedKeySpec spec =
                new PKCS8EncodedKeySpec(keyBytes);
        KeyFactory kf = KeyFactory.getInstance("RSA");
        return kf.generatePrivate(spec);
    }

    private static PublicKey getPublicKey(String filename) throws Exception {
        byte[] keyBytes = Files.readAllBytes(Paths.get(filename));

        X509EncodedKeySpec spec =
                new X509EncodedKeySpec(keyBytes);
        KeyFactory kf = KeyFactory.getInstance("RSA");
        return kf.generatePublic(spec);
    }
}
