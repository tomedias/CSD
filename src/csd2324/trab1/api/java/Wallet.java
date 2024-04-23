package csd2324.trab1.api.java;
import csd2324.trab1.server.java.Account;
import csd2324.trab1.server.java.SignedTransaction;
import csd2324.trab1.server.java.Transaction;

import java.util.List;

public interface Wallet {
    static String SERVICE_NAME = "wallet";

    Result<Boolean> transfer(SignedTransaction transaction);

    Result<Boolean> atomicTransfer(List<SignedTransaction> transactions);

    Result<Double> balance(String account);

    Result<List<Account>> ledger();

    Result<String> test();

    Result<Boolean> admin(Transaction transaction);
}
