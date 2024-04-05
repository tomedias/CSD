package csd2324.trab1.clients;

import csd2324.trab1.api.java.Wallet;

public class Client {
    public static void main(String[] args) {
        Wallet client = new RestWalletClient(args[0]);
        System.out.println(client.test().value());
    }
}
