package csd2324.trab1.server.java;


import csd2324.trab1.api.Account;
import csd2324.trab1.api.SignedTransaction;
import csd2324.trab1.api.Transaction;
import csd2324.trab1.api.java.Result;
import csd2324.trab1.api.java.Wallet;
import org.checkerframework.checker.units.qual.C;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import static csd2324.trab1.api.java.Result.ok;
import static csd2324.trab1.api.java.Result.error;
import static csd2324.trab1.api.java.Result.ErrorCode.FORBIDDEN;
import static csd2324.trab1.api.java.Result.ErrorCode.NOT_FOUND;


public class JavaWallet{

    final protected Map<String,Account> accountMap = new ConcurrentHashMap<>();
    List<String> adminAccounts = List.of("MFkwEwYHKoZIzj0CAQYIKoZIzj0DAQcDQgAEaZatK+wN0dHvQOPrFIIOkOoojw8LWCQYhdMO2xw0POF+Ph+mD/TiZG543+2Mplm2hjsQBHBgfrkrVmNbLH8TOQ==");
    public int state = 0;

    public int getState() {
        return state;
    }

    public void setState(int state) {
        this.state = state;
    }

    public JavaWallet(){
    }

    public JavaWallet(Map<String,Account> accountMap,int state){
        this.accountMap.putAll(accountMap);
        this.state = state;
    }

    private synchronized Result<Void> checkTransfer(SignedTransaction signed_transaction){
        if(signed_transaction==null) return error(FORBIDDEN);
        if(!signed_transaction.checkSignature()) return error(FORBIDDEN);
        Transaction transaction = signed_transaction.getTransaction();
        Account fromAccount = accountMap.get(transaction.getFrom());
        if(fromAccount==null) return error(NOT_FOUND);
        accountMap.computeIfAbsent(transaction.getTo(),new_account -> new Account(transaction.getTo()));
        if(fromAccount.getBalance() < transaction.getAmount()) return error(FORBIDDEN);
        return ok();
    }
    private synchronized void transferWithoutChecking(String from, String to, double amount){
        Account fromAccount = accountMap.get(from);
        Account toAccount = accountMap.get(to);
        fromAccount.removeBalance(amount);
        toAccount.addBalance(amount);
        System.out.println("From " + fromAccount.getBalance() + " to " + toAccount.getBalance() + " amount " + amount);
    }
    
    public synchronized Result<Void> transfer(SignedTransaction signed_transaction) {
        Result<Void> code = checkTransfer(signed_transaction);
        if(!code.isOK()) return code;
        Transaction transaction = signed_transaction.getTransaction();
        transferWithoutChecking(transaction.getFrom(),transaction.getTo(),transaction.getAmount());
        return ok();
    }

    
    public synchronized Result<Void> atomicTransfer(List<SignedTransaction> signed_transactions) {
        for(SignedTransaction transaction : signed_transactions){
            Result<Void> code = checkTransfer(transaction);
            if(!code.isOK()) return code;
        }
        System.out.println("All transactions are valid");
        for(SignedTransaction signed_transaction : signed_transactions){
            Transaction transaction = signed_transaction.getTransaction();
            transferWithoutChecking(transaction.getFrom(),transaction.getTo(),transaction.getAmount());
        }
        System.out.println("Money Wired");
        return ok();
    }

    
    public synchronized Result<Double> balance(String account) {
        Account fromAccount = accountMap.get(account);
        if(fromAccount==null) return ok(-1.0); // for debugging purposes
        return ok(fromAccount.getBalance());
    }

    
    public synchronized Result<String> test() {
        return ok("test");
    }

    
    public Result<Void> giveme(Transaction transaction) {
        String admin = transaction.getFrom();
        String accountID = transaction.getTo();
        double quantity = transaction.getAmount();
        if(!adminAccounts.contains(admin)){
            return error(FORBIDDEN);
        }
        Account account = accountMap.computeIfAbsent(accountID,new_account -> new Account(accountID));
        account.addBalance(quantity);
        return ok();

    }

    public synchronized JavaWallet copy(){
        return new JavaWallet(new HashMap<>(this.accountMap),this.state);
    }
}



