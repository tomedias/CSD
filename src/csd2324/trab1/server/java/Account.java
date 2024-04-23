package csd2324.trab1.server.java;

public class Account{

    private String id;
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

    protected void setId(String id) {
        this.id = id;
    }

    public double getBalance() {
        return balance;
    }

    protected void setBalance(double balance) {
        this.balance = balance;
    }


    protected void removeBalance(double balance) {
        this.balance-=balance;
    }


    protected void addBalance(double balance) {
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
