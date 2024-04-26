package csd2324.trab1.clients;

import csd2324.trab1.api.Account;
import csd2324.trab1.api.SignedTransaction;
import csd2324.trab1.api.Transaction;
import csd2324.trab1.api.java.Result;

import java.security.*;
import java.util.*;
import csd2324.trab1.utils.Secure;


public class Client{
    
    private Map<String, KeyPair> accountsMap; // Account ID -> KeyPair
    public final RestWalletClient restClient;

    public Client(String url){
        this.accountsMap = new HashMap<>();
        this.restClient = new RestWalletClient(url);
    }


    
    public Result<byte[]> transfer(Transaction transaction) {
        KeyPair keyPair = accountsMap.get(transaction.getFrom());
        PrivateKey privateKey = keyPair.getPrivate();
        return restClient.transfer(new SignedTransaction(privateKey,transaction));
    }


    
    public Result<byte[]> atomicTransfer(List<Transaction> transactions) {
        List<SignedTransaction> list_signed = new ArrayList<>();
        for(Transaction transaction : transactions){
            KeyPair keyPair = accountsMap.get(transaction.getFrom());
            PrivateKey privateKey = keyPair.getPrivate();
            list_signed.add(new SignedTransaction(privateKey,transaction));
        }
       return restClient.atomicTransfer(list_signed);
    }

    
    public Result<Double> balance(String account) {
        return restClient.balance(account);
    }

    
    public Result<byte[]> ledger() {
        return restClient.ledger();
    }

    
    public Result<byte[]> test() {
       return restClient.test();
    }


    
    public Result<Void> admin(Transaction transaction) { //Everybody can send admin requests they are just not verified.       There is still a secret since no SIGNATURE IMPLEMENTED //TODO

        return restClient.giveme(transaction);
    }

    public Account createAccount(){
        KeyPair keyPair = Secure.generateKeyPair();
        if(keyPair == null){
            return null;
        }
        Account account = new Account(Secure.publicKeyToString(keyPair));
        accountsMap.put(Secure.publicKeyToString(keyPair), keyPair);
        return account;
    }

    public KeyPair getKeyPair(String account){
        return accountsMap.get(account);
    }

    
}
