package csd2324.trab1.api.java;
import java.util.List;

import csd2324.trab1.api.Account;
import csd2324.trab1.api.SignedTransaction;
import csd2324.trab1.api.Transaction;

public interface Wallet {
    Result<Void> transfer(SignedTransaction transaction);

    Result<Void> atomicTransfer(List<SignedTransaction> transactions);

    Result<Double> balance(String account);

    Result<List<Account>> ledger();

    Result<String> test();

    Result<Void> giveme(Transaction transaction);
}
