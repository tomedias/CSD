package csd2324.trab1.api;

public class Account{

    private final String id;
    private double balance;

    public Account() {
        id=null;
        balance=0;
    }
    public Account(String id) {
        this.id = id;
        balance=0;
    }
    public Account(String id, double balance) {
        this.id = id;
        this.balance = balance;
    }

    public String getId() {
        return id;
    }


    public double getBalance() {
        return balance;
    }


    public void removeBalance(double balance) {
        this.balance-=balance;
    }


    public void addBalance(double balance) {
        this.balance+=balance;
    }

    @Override
    public String toString() {
        return "Account{" +
                "id='" + id + '\'' +
                ", balance=" + balance +
                '}';
    }
}
