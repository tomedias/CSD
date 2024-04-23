package csd2324.trab1.server.java;
import csd2324.trab1.utils.JSON;
import csd2324.trab1.utils.Secure;


import java.security.PrivateKey;

public class SignedTransaction {
    private final String signature;
    private final Transaction transaction;

    public SignedTransaction() {
        this.signature = null;
        this.transaction = null;
    }

    public SignedTransaction(PrivateKey privateKey, Transaction transaction) {
        try {
            this.signature = Secure.signData(JSON.encode(transaction).getBytes(), privateKey);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        this.transaction = transaction;
    }

    public String getSignature() {
        return signature;
    }

    public Transaction getTransaction() {
        return transaction;
    }

    public boolean checkSignature() {

        byte[] transactionBytes = JSON.encode(transaction).getBytes();
        try {
            return Secure.verifySignature(transactionBytes, signature, Secure.stringToPublicKey(transaction.getFrom()));
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
