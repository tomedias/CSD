package csd2324.trab1.api.java;
import csd2324.trab1.server.java.Account;
import csd2324.trab1.server.java.SignedTransaction;
import csd2324.trab1.server.java.Transaction;
import java.util.List;

public interface Wallet {
    Result<Void> transfer(SignedTransaction transaction);

    Result<Void> atomicTransfer(List<SignedTransaction> transactions);

    Result<Double> balance(String account);

    Result<List<Account>> ledger();

    Result<String> test();

    Result<Void> giveme(Transaction transaction);
}
