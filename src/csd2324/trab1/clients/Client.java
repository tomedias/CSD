package csd2324.trab1.clients;

import csd2324.trab1.api.java.Result;
import csd2324.trab1.api.java.Wallet;
import csd2324.trab1.server.java.Account;
import csd2324.trab1.server.java.Transaction;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.checkerframework.checker.units.qual.A;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.Security;
import java.security.spec.ECGenParameterSpec;
import java.util.Base64;
import csd2324.trab1.api.Secure;


public class Client{
    
    private Map<String, KeyPair> accountsMap; // Account ID -> KeyPair
    private final RestWalletClient restClient;

    public Client(String url){
        this.accountsMap = new HashMap<>();
        this.restClient = new RestWalletClient(url);
    }


    
    public Result<Boolean> transfer(Transaction transaction) {
        String signature = Secure.signTransaciton(transaction, accountsMap.get(transaction.getFrom()));
        return restClient.transfer(signature, transaction);
    }


    
    public Result<Boolean> atomicTransfer(List<Transaction> transactions,z) {
       return restClient.atomicTransfer(transactions);
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


    
    public Result<Boolean> admin(String command, List<String> args, String secret) { //Everybody can send admin requests they are just not verified.       There is still a secret since no SIGNATURE IMPLEMENTED //TODO

        return restClient.admin(command, args, secret);
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

    
}
