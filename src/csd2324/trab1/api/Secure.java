package csd2324.trab1.api;

import csd2324.trab1.server.java.Transaction;
import csd2324.trab1.utils.JSON;

import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.Security;
import java.security.Signature;
import java.security.spec.ECGenParameterSpec;
import java.util.Base64;

public class Secure {

    public static String signTransaciton(Transaction transaction, KeyPair keypair){//Secure clas
        try{
            byte[] stringTransaction =  JSON.encode(transaction).getBytes();
            String signature = signData(stringTransaction,keypair.getPrivate());
            return signature;   
        }catch(Exception e){
            e.printStackTrace();
        }
        return null;
        
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
