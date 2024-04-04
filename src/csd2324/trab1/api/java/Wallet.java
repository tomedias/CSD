package csd2324.trab1.api.java;

import csd2324.trab1.api.Signature;
import csd2324.trab1.server.java.Account;
import csd2324.trab1.server.java.Transaction;

import java.util.List;

public interface Wallet {
    static String SERVICE_NAME = "wallet";

    Result<Boolean> transfer(String from, String to, double amount, Signature signature);

    Result<Boolean> atomicTransfer(List<Transaction> transactions);

    Result<Double> balance(String account);

    Result<List<Account>> ledger();

    Result<String> test();
}
