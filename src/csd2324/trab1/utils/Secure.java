package csd2324.trab1.utils;

import java.security.*;
import java.security.spec.ECGenParameterSpec;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.util.Base64;

public class Secure {

    static {
        Security.addProvider(new org.bouncycastle.jce.provider.BouncyCastleProvider());
    }
    public static KeyPair generateKeyPair() {//TODO create a security class
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

    public static String publicKeyToString(KeyPair keyPair) { //TODO create a security class
        return Base64.getEncoder().encodeToString(keyPair.getPublic().getEncoded());
    }
    
    public static String privateKeyToString(KeyPair keyPair) {//TODO create a security class
        return Base64.getEncoder().encodeToString(keyPair.getPrivate().getEncoded());
    }

    public static PublicKey stringToPublicKey(String key) {//TODO create a security class
        try {

            byte[] byteKey = Base64.getDecoder().decode(key);
            return KeyFactory.getInstance("ECDSA").generatePublic(new X509EncodedKeySpec(byteKey));
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public static PrivateKey stringToPrivateKey(String key) {//TODO create a security class
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
}
