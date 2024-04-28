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


    
    public Result<byte[]> transfer(Transaction transaction, int op_number) {
        KeyPair keyPair = accountsMap.get(transaction.getFrom());
        PrivateKey privateKey = keyPair.getPrivate();
        return restClient.transfer(new SignedTransaction(privateKey,transaction),op_number);
    }


    
    public Result<byte[]> atomicTransfer(List<Transaction> transactions, int op_number) {
        List<SignedTransaction> list_signed = new ArrayList<>();
        for(Transaction transaction : transactions){
            KeyPair keyPair = accountsMap.get(transaction.getFrom());
            PrivateKey privateKey = keyPair.getPrivate();
            list_signed.add(new SignedTransaction(privateKey,transaction));
        }
       return restClient.atomicTransfer(list_signed,op_number);
    }

    
    public Result<byte[]> balance(String account, int op_number) {
        return restClient.balance(account,op_number);
    }

    
    public Result<byte[]> ledger(int op_number) {
        return restClient.ledger(op_number);
    }

    
    public Result<byte[]> test() {
       return restClient.test();
    }


    
    public Result<byte[]> admin(Transaction transaction, int op_number) { //Everybody can send admin requests they are just not verified.       There is still a secret since no SIGNATURE IMPLEMENTED //TODO

        return restClient.giveme(transaction,op_number);
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
