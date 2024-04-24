package csd2324.trab1.server.java;


public class Transaction{
    private final String from;
    private final String to;
    private final double amount;



    public Transaction() {
        this.from = null;
        this.to = null;
        this.amount = 0;

    }
    public Transaction(String from, String to, double amount) {
        this.from = from;
        this.to = to;
        this.amount = amount;

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


}
