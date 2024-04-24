package csd2324.trab1;

import csd2324.trab1.clients.Client;
import csd2324.trab1.server.java.Account;
import csd2324.trab1.server.java.Transaction;

import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;


public class test {

    private static final String admin_id = "MFkwEwYHKoZIzj0CAQYIKoZIzj0DAQcDQgAEaZatK+wN0dHvQOPrFIIOkOoojw8LWCQYhdMO2xw0POF+Ph+mD/TiZG543+2Mplm2hjsQBHBgfrkrVmNbLH8TOQ==";
    private static final Map<String, Account> map = new HashMap<>();
    public static void main(String[] args) {
        Client client = new Client("http://localhost:3456/rest");

        while (true){
            System.out.println("Next command:");
            String command = new Scanner(System.in).nextLine();
            switch (command){
                case "create" -> {
                    System.out.println("Enter the account name:");
                    String name = new Scanner(System.in).nextLine();
                    map.put(name,client.createAccount());
                }
                case "admin" -> {
                    System.out.println("Enter the account name:");
                    String name = new Scanner(System.in).nextLine();
                    System.out.println("Enter the amount:");
                    double amount = new Scanner(System.in).nextDouble();
                    System.out.println(client.admin(new Transaction(admin_id,map.get(name).getId(),amount)));
                }
                case "balance" -> {
                    System.out.println("Enter the account name:");
                    String name = new Scanner(System.in).nextLine();
                    System.out.println(client.balance(map.get(name).getId()));
                }
                case "ledger" -> {
                    System.out.println(client.ledger());
                }
                case "transfer" -> {
                    System.out.println("Enter the account name of the sender:");
                    String from = new Scanner(System.in).nextLine();
                    System.out.println("Enter the account name of the receiver:");
                    String to = new Scanner(System.in).nextLine();
                    System.out.println("Enter the amount:");
                    double amount = new Scanner(System.in).nextDouble();
                    System.out.println(client.transfer(new Transaction(map.get(from).getId(),map.get(to).getId(),amount)));
                }
                case "test" -> {
                    System.out.println(client.test());
                }
                default -> System.out.println("Unexpected value: " + command);
            }
        }



    }

}
