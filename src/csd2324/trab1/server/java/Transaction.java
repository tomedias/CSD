package csd2324.trab1.server.java;

import csd2324.trab1.api.Signature;

public class Transaction{
    private String from;
    private String to;
    private double amount;
    private Signature signature;


    public Transaction() {
        this.from = null;
        this.to = null;
        this.amount = 0;
        this.signature = null;
    }
    public Transaction(String from, String to, double amount, Signature signature) {
        this.from = from;
        this.to = to;
        this.amount = amount;
        this.signature = signature;
    }

    public String getFrom() {
        return from;
    }

    public String getTo() {
        return to;
    }

    public double getAmount() {
        return amount;
    }

    public Signature getSignature() {
        return signature;
    }
}
