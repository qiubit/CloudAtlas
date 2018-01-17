package pl.edu.mimuw.cloudatlas.signer;

import pl.edu.mimuw.cloudatlas.model.Query;

import javax.crypto.Cipher;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.security.MessageDigest;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.util.Arrays;

public class QuerySigner {
    private final static String DIGEST_ALGORITHM = "SHA-1";
    private final static String ENCRYPTION_ALGORITHM = "RSA";

    private enum RequestType {
        INSTALL,
        UNINSTALL
    }

    private static class QueryToSign implements Serializable {
        public final Query query;
        public final RequestType requestType;

        public QueryToSign(Query query, RequestType requestType) {
            this.query = query;
            this.requestType = requestType;
        }

        public String toString() {
            return "QUERY:" + query.name.toString() + "|" + query.query.toString() + "|" + requestType.toString();
        }

    }

    public static byte[] signInstallQuery(Query query, PrivateKey privateKey) throws Exception {
        return signQuery(query, RequestType.INSTALL, privateKey);
    }

    public static byte[] signUninstallQuery(Query query, PrivateKey privateKey) throws Exception {
        return signQuery(query, RequestType.UNINSTALL, privateKey);
    }

    public static boolean verifyInstallSignature(Query query, byte[] signature, PublicKey publicKey) throws Exception {
        return verifySignature(query, RequestType.INSTALL, signature, publicKey);
    }


    public static boolean verifyUninstallSignature(Query query, byte[] signature, PublicKey publicKey) throws Exception {
        return verifySignature(query, RequestType.UNINSTALL, signature, publicKey);
    }

    private static byte[] signQuery(Query query, RequestType requestType, PrivateKey privateKey) throws Exception {
        byte[] hash = getQueryHash(query, requestType);
        Cipher signCipher = Cipher.getInstance(ENCRYPTION_ALGORITHM);
        signCipher.init(Cipher.ENCRYPT_MODE, privateKey);
        return signCipher.doFinal(hash);
    }

    private static boolean verifySignature(Query query, RequestType requestType, byte[] signature, PublicKey publicKey) throws Exception {
        Cipher verifyCipher = Cipher.getInstance(ENCRYPTION_ALGORITHM);
        verifyCipher.init(Cipher.DECRYPT_MODE, publicKey);
        byte[] decryptedBytes = verifyCipher.doFinal(signature);
        byte[] hash = getQueryHash(query, requestType);
        return Arrays.equals(hash, decryptedBytes);
    }

    private static byte[] getQueryHash(Query query, RequestType requestType) throws Exception {
        MessageDigest digestGenerator = MessageDigest.getInstance(DIGEST_ALGORITHM);
        return digestGenerator.digest((new QueryToSign(query, requestType)).toString().getBytes());
    }

}
