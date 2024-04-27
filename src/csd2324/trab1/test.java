package csd2324.trab1;

import com.google.gson.reflect.TypeToken;
import csd2324.trab1.api.Account;
import csd2324.trab1.api.SignedMessage;
import csd2324.trab1.api.Transaction;
import csd2324.trab1.api.java.Result;
import csd2324.trab1.clients.Client;
import csd2324.trab1.utils.JSON;
import csd2324.trab1.utils.Secure;

import java.io.*;
import java.nio.ByteBuffer;
import java.security.KeyPair;
import java.security.PublicKey;
import java.util.*;


public class test {
    private static final ArrayList<PublicKey> publicKeys = new ArrayList<>(4);
    private static final String admin_id = "MFkwEwYHKoZIzj0CAQYIKoZIzj0DAQcDQgAEaZatK+wN0dHvQOPrFIIOkOoojw8LWCQYhdMO2xw0POF+Ph+mD/TiZG543+2Mplm2hjsQBHBgfrkrVmNbLH8TOQ==";
    private static final Map<String, Account> map = new HashMap<>();
    private static final Random random = new Random();
    public static void main(String[] args) {

        for (int i = 1; i <= 4; i++) {
            try {
                publicKeys.add(Secure.stringToPublicKey(new BufferedReader(new FileReader(String.format("./tls/rest%d/publickey",i))).readLine()));
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        System.setProperty("javax.net.ssl.trustStore", "tls/truststore");
        System.setProperty("javax.net.ssl.trustStorePassword","changeit");
        Client client = new Client("https://localhost:3456/rest");

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

                    Result<byte[]> result = client.admin(new Transaction(admin_id,map.get(name).getId(),amount));
                    if (!result.isOK()){
                        System.out.println("Error");
                        break;
                    }
                    SignedMessage<Void> response = getResponse(result.value());
                    System.out.println(response.getResult());
                }
                case "balance" -> {
                    System.out.println("Enter the account name:");
                    String name = new Scanner(System.in).nextLine();
                    Result<byte[]> result = client.balance(map.get(name).getId());
                    if (!result.isOK()){
                        System.out.println("Error");
                        break;
                    }
                    SignedMessage<Double> response = getResponse(result.value());
                    System.out.println(response.getResult());
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
                    Result<byte[]> result = client.transfer(new Transaction(map.get(from).getId(),map.get(to).getId(),amount));
                    if (!result.isOK()){
                        System.out.println("Error");
                        break;
                    }
                    SignedMessage<Void> response = getResponse(result.value());
                    System.out.println(response.getResult());
                }
                case "test" -> {
                    Result<byte[]> result = client.test();
                    if (!result.isOK()){
                        System.out.println("Error");
                        break;
                    }
                    SignedMessage<String> response = getResponse(result.value());
                    System.out.println(response.getResult());
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
                    Result<byte[]> result = client.atomicTransfer(transactions);
                    if (!result.isOK()){
                        System.out.println("Error");
                        break;
                    }
                    SignedMessage<Void> response = getResponse(result.value());
                    System.out.println(response.getResult());
                }
                case "spam" -> {
                    System.out.println("Enter the account name of the receiver:");
                    String to = new Scanner(System.in).nextLine();
                    for(int i=0; i< 100; i++){
                        final int thread_id = i;
                        new Thread(() -> {
                            int nr = random.nextInt(1,5);
                            int port = 3455 + nr;
                            Client client_thread = new Client(String.format("https://localhost:%d/rest",port));
                            Result<byte[]> result = client_thread.admin(new Transaction(admin_id,map.get(to).getId(),1));
                            if (!result.isOK()){
                                System.out.println("Error");
                            }
                            SignedMessage<Void> response = getResponse(result.value());
                            System.out.println(thread_id +": " + response.getResult());
                        }).start();
                    }

                }
                default -> System.out.println("Unexpected value: " + command);
            }
        }
    }

    private static <T> SignedMessage<T> getResponse(byte[] content) {
        try {
            String json = new String(CheckSignature(content));
            return JSON.decode(json,new TypeToken<SignedMessage<T>>() {});
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private static byte[] CheckSignature(byte[] command) throws Exception {
        ByteBuffer buffer = ByteBuffer.wrap(command);
        int nr = buffer.getInt();
        int l = buffer.getInt();
        byte[] request = new byte[l];
        buffer.get(request);
        l = buffer.getInt();
        byte[] signature = new byte[l];
        buffer.get(signature);
        String sig = new String(signature);
        if (!Secure.verifySignature(request, sig, publicKeys.get(nr-1))) {
            System.out.println("Client sent invalid signature!");
            System.exit(0);
        }
        return request;
    }

}
