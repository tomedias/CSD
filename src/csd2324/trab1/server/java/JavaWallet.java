package csd2324.trab1.server.java;


import csd2324.trab1.api.java.Result;
import csd2324.trab1.api.java.Wallet;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import static csd2324.trab1.api.java.Result.ok;
import static csd2324.trab1.api.java.Result.error;
import static csd2324.trab1.api.java.Result.ErrorCode.FORBIDDEN;
import static csd2324.trab1.api.java.Result.ErrorCode.NOT_FOUND;


public class JavaWallet implements Wallet {

    final protected Map<String,Account> accountMap = new HashMap<>();
    List<String> adminAccounts = List.of("MFkwEwYHKoZIzj0CAQYIKoZIzj0DAQcDQgAEaZatK+wN0dHvQOPrFIIOkOoojw8LWCQYhdMO2xw0POF+Ph+mD/TiZG543+2Mplm2hjsQBHBgfrkrVmNbLH8TOQ==");

    private Result<Boolean> checkTransfer(SignedTransaction signed_transaction){
        if(signed_transaction==null) return error(FORBIDDEN);
        if(!signed_transaction.checkSignature()) return error(FORBIDDEN);
        Transaction transaction = signed_transaction.getTransaction();
        Account fromAccount = accountMap.get(transaction.getFrom());
        if(fromAccount==null) return error(NOT_FOUND);
        accountMap.computeIfAbsent(transaction.getTo(),new_account -> new Account(transaction.getTo()));
        if(fromAccount.getBalance() < transaction.getAmount()) return error(FORBIDDEN);
        return ok(true);
    }
    private void transferWithoutChecking(String from, String to, double amount){
        Account fromAccount = accountMap.get(from);
        Account toAccount = accountMap.get(to);
        fromAccount.removeBalance(amount);
        toAccount.addBalance(amount);
    }
    @Override
    public Result<Boolean> transfer(SignedTransaction signed_transaction) {
        Result<Boolean> code = checkTransfer(signed_transaction);
        Transaction transaction = signed_transaction.getTransaction();
        if(!code.isOK()) return code;
        transferWithoutChecking(transaction.getFrom(),transaction.getTo(),transaction.getAmount());
        return ok(true);
    }

    @Override
    public Result<Boolean> atomicTransfer(List<SignedTransaction> signed_transactions) {
        for(SignedTransaction transaction : signed_transactions){
            Result<Boolean> code = checkTransfer(transaction);
            if(!code.isOK()) return code;
        }
        for(SignedTransaction signed_transaction : signed_transactions){
            Transaction transaction = signed_transaction.getTransaction();
            transferWithoutChecking(transaction.getFrom(),transaction.getFrom(),transaction.getAmount());
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
        return ok("test");
    }

    @Override
    public Result<Boolean> admin(Transaction transaction) {
        String admin = transaction.getFrom();
        String accountID = transaction.getTo();
        double quantity = transaction.getAmount();
        if(!adminAccounts.contains(admin)){
            return error(FORBIDDEN);
        }
        Account account = accountMap.computeIfAbsent(accountID,new_account -> new Account(accountID));
        account.addBalance(quantity);
        return ok(true);

    }
}



