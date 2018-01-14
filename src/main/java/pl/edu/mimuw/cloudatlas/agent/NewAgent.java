package pl.edu.mimuw.cloudatlas.agent;

import pl.edu.mimuw.cloudatlas.modules.FetcherModule;
import pl.edu.mimuw.cloudatlas.modules.Module;
import pl.edu.mimuw.cloudatlas.modules.TimerModule;
import pl.edu.mimuw.cloudatlas.modules.ZMIHolderModule;

import java.nio.file.Files;
import java.nio.file.Paths;
import java.security.KeyFactory;
import java.security.PublicKey;
import java.security.spec.X509EncodedKeySpec;

public class NewAgent {
    public static void main(String[] args) throws Exception {

        if (args.length != 1) {
            System.out.println("Usage: ./java <signer-public-key-path>");
            return;
        }

        Module ZMIHolder = new ZMIHolderModule(ZMIHolderModule.createTestHierarchy(), getPublicKey(args[0]));
        Module Timer = new TimerModule();
        Module Fetcher = new FetcherModule();
    }

    private static PublicKey getPublicKey(String filename) throws Exception {
        byte[] keyBytes = Files.readAllBytes(Paths.get(filename));

        X509EncodedKeySpec spec =
                new X509EncodedKeySpec(keyBytes);
        KeyFactory kf = KeyFactory.getInstance("RSA");
        return kf.generatePublic(spec);
    }
}
