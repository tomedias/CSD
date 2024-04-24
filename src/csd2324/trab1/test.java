package csd2324.trab1;

import bftsmart.tom.MessageContext;
import csd2324.trab1.api.java.Result;
import csd2324.trab1.clients.Client;
import csd2324.trab1.server.java.Account;
import csd2324.trab1.server.java.Transaction;
import csd2324.trab1.utils.JSON;

import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;


public class test {

    private static final String admin_id = "MFkwEwYHKoZIzj0CAQYIKoZIzj0DAQcDQgAEaZatK+wN0dHvQOPrFIIOkOoojw8LWCQYhdMO2xw0POF+Ph+mD/TiZG543+2Mplm2hjsQBHBgfrkrVmNbLH8TOQ==";
    private static final Map<String, Account> map = new HashMap<>();
    public static void main(String[] args) {


        System.setProperty("javax.net.ssl.trustStore", "tls/truststore");
        System.setProperty("javax.net.ssl.trustStorePassword","changeit");
        Client client = new Client("https://localhost");

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
                    Result<String> result = client.test();
                    if(result.isOK()){

                        MessageContext messageContext = JSON.decode(result.value(),MessageContext.class);

                        System.out.println(messageContext.getConsensusId());
                        System.out.println(messageContext.getOperationId());
                        System.out.println(messageContext.getSender());
                        System.out.println(messageContext.getSequence());
                        System.out.println(messageContext.getTimestamp());
                        System.out.println(messageContext.getProof());
                        System.out.println(messageContext.getSignature());
                        System.out.println(messageContext.getNumOfNonces());
                    }


                }
                case "atomic" -> {
                    System.out.println("Enter the account name of the sender:");
                    String from = new Scanner(System.in).nextLine();
                    System.out.println("Enter the account name of the receiver:");
                    String to = new Scanner(System.in).nextLine();
                    List<Transaction> transactions = new ArrayList<>();
                    for (int i =0 ; i< 50; i++){
                        transactions.add(new Transaction(map.get(from).getId(),map.get(to).getId(),1));
                    }
                    System.out.println(client.atomicTransfer(transactions));
                }
                case "spam" -> {

                    System.out.println("Enter the account name of the receiver:");
                    String to = new Scanner(System.in).nextLine();
                    for(int i=0; i< 50; i++){
                        new Thread(() -> {
                            System.out.println(client.admin(new Transaction(admin_id,map.get(to).getId(),1)));
                        }).start();

                    }

                }
                default -> System.out.println("Unexpected value: " + command);
            }
        }



    }

}
