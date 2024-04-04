package csd2324.trab1.server.java;

import csd2324.trab1.api.Signature;
import csd2324.trab1.api.java.Result;
import csd2324.trab1.api.java.Wallet;


import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static csd2324.trab1.api.java.Result.ok;
import static csd2324.trab1.api.java.Result.error;
import static csd2324.trab1.api.java.Result.ErrorCode.FORBIDDEN;
import static csd2324.trab1.api.java.Result.ErrorCode.NOT_FOUND;


public class JavaWallet implements Wallet {

    final protected Map<String,Account> accountMap = new HashMap<>();


    private Result<Boolean> checkTransfer(String from, String to, double amount, Signature signature){
        Account fromAccount = accountMap.get(from);
        if(fromAccount==null) return error(NOT_FOUND);
        Account toAccount = accountMap.get(to);
        if(toAccount==null) return error(NOT_FOUND);
        if(fromAccount.getBalance() < amount) return error(FORBIDDEN);
        if(!checkSignature(fromAccount,signature)) return error(FORBIDDEN);
        return ok(true);
    }
    private void transferWithoutChecking(String from, String to, double amount, Signature signature){
        Account fromAccount = accountMap.get(from);
        Account toAccount = accountMap.get(to);
        fromAccount.removeBalance(amount);
        toAccount.addBalance(amount);
    }
    @Override
    public Result<Boolean> transfer(String from, String to, double amount, Signature signature) {
        Result<Boolean> code = checkTransfer(from,to,amount,signature);
        if(!code.isOK()) return code;
        transferWithoutChecking(from,to,amount,signature);
        return ok(true);
    }

    @Override
    public Result<Boolean> atomicTransfer(List<Transaction> transactions) {
        for(Transaction transaction : transactions){
            Result<Boolean> code = checkTransfer(transaction.getFrom(),transaction.getFrom(),transaction.getAmount(), transaction.getSignature());
            if(!code.isOK()) return code;
        }
        for(Transaction transaction : transactions){
            transferWithoutChecking(transaction.getFrom(),transaction.getFrom(),transaction.getAmount(), transaction.getSignature());
        }
        return ok(true);
    }

    @Override
    public Result<Double> balance(String account) {
        Account fromAccount = accountMap.get(account);
        if(fromAccount==null) return ok(0.0);
        return ok(fromAccount.getBalance());
    }

    @Override
    public Result<List<Account>> ledger() {
        return ok(accountMap.values().stream().toList());
    }

    @Override
    public Result<String> test() {
        accountMap.put("oi",new Account("Oi",192.23));
        return ok("test");
    }

    private boolean checkSignature(Account from, Signature sig){
        return true;
    }
}
