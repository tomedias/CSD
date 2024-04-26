package csd2324.trab1.api.java;
import java.util.List;

import csd2324.trab1.api.Account;
import csd2324.trab1.api.SignedTransaction;
import csd2324.trab1.api.Transaction;

public interface Wallet {
    Result<byte[]> transfer(SignedTransaction transaction);

    Result<byte[]> atomicTransfer(List<SignedTransaction> transactions);

    Result<byte[]> balance(String account);

    Result<byte[]> ledger();

    Result<byte[]> test();

    Result<byte[]> giveme(Transaction transaction);
}
