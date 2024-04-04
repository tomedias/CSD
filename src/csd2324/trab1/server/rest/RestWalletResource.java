package csd2324.trab1.server.rest;

import csd2324.trab1.api.Signature;
import csd2324.trab1.api.java.Wallet;
import csd2324.trab1.api.rest.WalletService;
import csd2324.trab1.server.java.Account;
import csd2324.trab1.server.java.JavaWallet;
import csd2324.trab1.server.java.Transaction;
import jakarta.inject.Singleton;

import java.util.List;

@Singleton
public class RestWalletResource extends RestResource implements WalletService {

    final Wallet impl;

    public RestWalletResource() {
        this.impl = new JavaWallet();
    }

    @Override
    public boolean transfer(String from, String to, double amount, Signature signature) {
        return super.fromJavaResult(impl.transfer(from,to,amount,signature));
    }

    @Override
    public boolean atomicTransfer(List<Transaction> transactions) {
        return super.fromJavaResult(impl.atomicTransfer(transactions));
    }

    @Override
    public double balance(String account) {
        return super.fromJavaResult(impl.balance(account));
    }

    @Override
    public List<Account> ledger() {
        return super.fromJavaResult(impl.ledger());
    }

    @Override
    public String test() {
        return super.fromJavaResult(impl.test());
    }
}
