package csd2324.trab1.utils;

import java.nio.ByteBuffer;
import java.security.*;
import java.security.spec.ECGenParameterSpec;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.util.ArrayList;
import java.util.Base64;

public class Secure {

    static {
        Security.addProvider(new org.bouncycastle.jce.provider.BouncyCastleProvider());
    }
    public static KeyPair generateKeyPair() {
        try {
            KeyPairGenerator keyPairGenerator = KeyPairGenerator.getInstance("ECDSA", "BC");
            ECGenParameterSpec ecSpec = new ECGenParameterSpec("prime256v1");
            keyPairGenerator.initialize(ecSpec);
            return keyPairGenerator.generateKeyPair();
        } catch (NoSuchAlgorithmException | NoSuchProviderException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public static String publicKeyToString(KeyPair keyPair) {
        return Base64.getEncoder().encodeToString(keyPair.getPublic().getEncoded());
    }
    
    public static String privateKeyToString(KeyPair keyPair) {
        return Base64.getEncoder().encodeToString(keyPair.getPrivate().getEncoded());
    }

    public static PublicKey stringToPublicKey(String key) {
        try {

            byte[] byteKey = Base64.getDecoder().decode(key);
            return KeyFactory.getInstance("ECDSA").generatePublic(new X509EncodedKeySpec(byteKey));
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public static PrivateKey stringToPrivateKey(String key) {
        try {
            byte[] byteKey = Base64.getDecoder().decode(key);
            return KeyFactory.getInstance("ECDSA").generatePrivate(new PKCS8EncodedKeySpec(byteKey));
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public static String signData(byte[] data, PrivateKey privateKey) throws Exception {
        Signature signature = Signature.getInstance("SHA256withECDSA");
        signature.initSign(privateKey);
        signature.update(data);
        byte[] digitalSignature = signature.sign();
        return Base64.getEncoder().encodeToString(digitalSignature);
    }
    public static boolean verifySignature(byte[] data, String signatureStr, PublicKey publicKey) throws Exception {
        Signature signature = Signature.getInstance("SHA256withECDSA");
        signature.initVerify(publicKey);
        signature.update(data);
        byte[] digitalSignature = Base64.getDecoder().decode(signatureStr);
        return signature.verify(digitalSignature);
    }


    public static byte[] hash(byte[] data) throws Exception {
        MessageDigest md = MessageDigest.getInstance("SHA-256");
        md.update(data);
        return md.digest();
    }

    public static byte[] CheckSignature(byte[] command, ArrayList<PublicKey> publicKeys) {
        try{
            ByteBuffer buffer = ByteBuffer.wrap(command);
            int nr = buffer.getInt();
            int l = buffer.getInt();
            byte[] request = new byte[l];
            buffer.get(request);
            l = buffer.getInt();
            byte[] signature = new byte[l];
            buffer.get(signature);
            String sig = new String(signature);
            PublicKey key = publicKeys.get(nr-1);
            if (!Secure.verifySignature(request, sig, key)) {
                System.out.println("Client sent invalid signature!");
                System.exit(0);
            }
            return request;
        }catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

}
