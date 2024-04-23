package csd2324.trab1.clients;

import csd2324.trab1.api.java.Result;
import csd2324.trab1.server.java.Account;
import csd2324.trab1.server.java.SignedTransaction;
import csd2324.trab1.server.java.Transaction;

import java.security.*;
import java.util.*;
import csd2324.trab1.utils.Secure;



public class Client{
    
    private Map<String, KeyPair> accountsMap; // Account ID -> KeyPair
    private final RestWalletClient restClient;

    public Client(String url){
        this.accountsMap = new HashMap<>();
        this.restClient = new RestWalletClient(url);
    }


    
    public Result<Boolean> transfer(Transaction transaction) {
        KeyPair keyPair = accountsMap.get(transaction.getFrom());
        PrivateKey privateKey = keyPair.getPrivate();
        return restClient.transfer(new SignedTransaction(privateKey,transaction));
    }


    
    public Result<Boolean> atomicTransfer(List<Transaction> transactions) {
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

    
    public Result<List<Account>> ledger() {
        return restClient.ledger();
    }

    
    public Result<String> test() {
       return restClient.test();
    }


    
    public Result<Boolean> admin(Transaction transaction) { //Everybody can send admin requests they are just not verified.       There is still a secret since no SIGNATURE IMPLEMENTED //TODO

        return restClient.admin(transaction);
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
