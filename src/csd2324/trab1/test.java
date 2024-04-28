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
import java.security.KeyPair;
import java.security.PublicKey;
import java.util.*;
import java.util.concurrent.CountDownLatch;


public class test {
    private static final ArrayList<PublicKey> publicKeys = new ArrayList<>(4);
    private static final String admin_id = "MFkwEwYHKoZIzj0CAQYIKoZIzj0DAQcDQgAEaZatK+wN0dHvQOPrFIIOkOoojw8LWCQYhdMO2xw0POF+Ph+mD/TiZG543+2Mplm2hjsQBHBgfrkrVmNbLH8TOQ==";
    private static final Map<String, Account> map = new HashMap<>();
    private static final Random random = new Random();
    public static void main(String[] args) throws Exception {

        for (int i = 1; i <= 4; i++) {
            try {
                publicKeys.add(Secure.stringToPublicKey(new BufferedReader(new FileReader(String.format("./tls/rest%d/publickey",i))).readLine()));
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        System.setProperty("javax.net.ssl.trustStore", "tls/truststore");
        System.setProperty("javax.net.ssl.trustStorePassword","changeit");
        List<Client> clients = new ArrayList<>();
        for (int i = 1; i <= 4; i++) {
            clients.add(new Client(String.format("https://localhost:%d/rest",3455+i)));
        }

        while (true){
            System.out.println("Next command:");
            String command = new Scanner(System.in).nextLine();
            Collections.shuffle(clients);
            switch (command){
                case "create" -> {
                    System.out.println("Enter the account name:");
                    String name = new Scanner(System.in).nextLine();
                    map.put(name,clients.getFirst().createAccount());
                    KeyPair key = clients.getFirst().getKeyPair(map.get(name).getId());
                    for(int i =1 ; i< clients.size(); i++){
                        clients.get(i).addAccount(map.get(name).getId(),key);
                    }
                }
                case "admin" -> {
                    final int OperationNumber = random.nextInt(Integer.MIN_VALUE,Integer.MAX_VALUE);
                    System.out.println("Enter the account name:");
                    String name = new Scanner(System.in).nextLine();
                    System.out.println("Enter the amount:");
                    double amount = new Scanner(System.in).nextDouble();

                    Result<byte[]> result = clients.getFirst().admin(new Transaction(admin_id,map.get(name).getId(),amount),OperationNumber);
                    if (!result.isOK()){
                        System.out.println("Error");
                        break;
                    }
                    SignedMessage<Void> response = getResponse(result.value());
                    checkWithOtherServers(clients, response.getOp_number(), response.getLedger_used_hash());

                }
                case "balance" -> {
                    final int OperationNumber = random.nextInt(Integer.MIN_VALUE,Integer.MAX_VALUE);
                    System.out.println("Enter the account name:");
                    String name = new Scanner(System.in).nextLine();
                    Result<byte[]> result = clients.getFirst().balance(map.get(name).getId(),OperationNumber);
                    if (!result.isOK()){
                        System.out.println("Error");
                        break;
                    }
                    SignedMessage<Double> response = getResponse(result.value());
                    System.out.println(response.getResult());
                    checkWithOtherServers(clients, response.getOp_number(), response.getLedger_used_hash());
                }
                case "transfer" -> {
                    final int OperationNumber = random.nextInt(Integer.MIN_VALUE,Integer.MAX_VALUE);
                    System.out.println("Enter the account name of the sender:");
                    String from = new Scanner(System.in).nextLine();
                    System.out.println("Enter the account name of the receiver:");
                    String to = new Scanner(System.in).nextLine();
                    System.out.println("Enter the amount:");
                    double amount = new Scanner(System.in).nextDouble();
                    Result<byte[]> result = clients.getFirst().transfer(new Transaction(map.get(from).getId(),map.get(to).getId(),amount),OperationNumber);
                    if (!result.isOK()){
                        System.out.println("Error");
                        break;
                    }
                    SignedMessage<Void> response = getResponse(result.value());
                    System.out.println(response.getResult());
                    checkWithOtherServers(clients, response.getOp_number(), response.getLedger_used_hash());
                }
                case "test" -> {
                    Result<byte[]> result = clients.getFirst().test();
                    if (!result.isOK()){
                        System.out.println("Error");
                        break;
                    }
                    String response = new String(Secure.CheckSignature(result.value(),publicKeys));
                    System.out.println(response);

                }
                case "atomic" -> {
                    final int OperationNumber = random.nextInt(Integer.MIN_VALUE,Integer.MAX_VALUE);
                    System.out.println("Enter the account name of the sender:");
                    String from = new Scanner(System.in).nextLine();
                    System.out.println("Enter the account name of the receiver:");
                    String to = new Scanner(System.in).nextLine();
                    List<Transaction> transactions = new ArrayList<>();
                    for (int i =0 ; i< 50; i++){
                        transactions.add(new Transaction(map.get(from).getId(),map.get(to).getId(),1));
                    }
                    Result<byte[]> result = clients.getFirst().atomicTransfer(transactions,OperationNumber);
                    if (!result.isOK()){
                        System.out.println("Error");
                        break;
                    }
                    SignedMessage<Void> response = getResponse(result.value());
                    System.out.println(response.getResult());
                    checkWithOtherServers(clients, response.getOp_number(), response.getLedger_used_hash());
                }
                case "spam" -> {
                    System.out.println("Enter the account name of the receiver:");
                    String to = new Scanner(System.in).nextLine();
                    CountDownLatch latch = new CountDownLatch(10);
                    for(int i=0; i< 10; i++){
                        final int thread_id = i;
                        new Thread(() -> {
                            final int OperationNumber = random.nextInt(Integer.MIN_VALUE,Integer.MAX_VALUE);
                            Result<byte[]> result = clients.getFirst().admin(new Transaction(admin_id,map.get(to).getId(),1),OperationNumber);
                            if (!result.isOK()){
                                System.out.println("Error");
                            }
                            SignedMessage<Void> response = getResponse(result.value());
                            System.out.println(thread_id +": " + response.getResult());
                            checkWithOtherServers(clients, response.getOp_number(), response.getLedger_used_hash());
                            latch.countDown();
                        }).start();
                    }
                    latch.await();
                }
                default -> System.out.println("Unexpected value: " + command);
            }
        }
    }

    private static void checkWithOtherServers(List<Client> clients, int operationNumber, byte[] ledgerUsedHash)  {
        try{

            Result<byte[]> result_ledger = clients.get(1).ledger(operationNumber);
            if (!result_ledger.isOK()){
                System.out.println("Not the same");
                return;
            }
            byte[] ledger = Secure.CheckSignature(result_ledger.value(),publicKeys);
            System.out.println(Arrays.equals(ledger, ledgerUsedHash)?"Ledger is the same":"Ledger is different");

        }catch (Exception e){
            e.printStackTrace();
        }

    }

    private static <T> SignedMessage<T> getResponse(byte[] content) {
        try {
            String json = new String(Secure.CheckSignature(content,publicKeys));
            return JSON.decode(json,new TypeToken<SignedMessage<T>>() {});
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }



}
