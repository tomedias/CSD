package csd2324.trab1.server.rest;

import bftsmart.tom.ServiceProxy;
import com.google.gson.reflect.TypeToken;
import csd2324.trab1.api.Signature;
import csd2324.trab1.api.java.Wallet;
import csd2324.trab1.api.rest.WalletService;
import csd2324.trab1.server.java.Account;
import csd2324.trab1.server.java.JavaWallet;
import csd2324.trab1.server.java.Transaction;
import csd2324.trab1.utils.JSON;
import jakarta.inject.Singleton;


import java.io.*;
import java.util.Collections;
import java.util.List;
import java.util.logging.Logger;



@Singleton
public class RestWalletResource implements WalletService {
    private static Logger Log = Logger.getLogger(RestWalletResource.class.getName());
    final Wallet impl;
    int process = 0;

    public RestWalletResource() {
        this.impl = new JavaWallet();
    }

    @Override
    public boolean transfer(String from, String to, double amount, Signature signature) {
        process++;
        ServiceProxy counterProxy = new ServiceProxy(process);

            ByteArrayOutputStream out = new ByteArrayOutputStream(1000);
            try {
                new DataOutputStream(out).writeUTF("transfer");
                new DataOutputStream(out).writeUTF(from);
                new DataOutputStream(out).writeUTF(to);
                new DataOutputStream(out).writeDouble(amount);
                new DataOutputStream(out).writeUTF(signature.toString());
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
                byte[] reply = counterProxy.invokeOrdered(out.toByteArray()); //magic happens here
                if(reply != null) {
                    try {
                        return new DataInputStream(new ByteArrayInputStream(reply)).readBoolean();
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                } else {
                    System.out.println(", ERROR! Exiting.");
                    return false;
                }
    }

    @Override
    public boolean atomicTransfer(List<Transaction> transactions) {
        for(Transaction transaction : transactions){
            transfer(transaction.getFrom(), transaction.getTo(), transaction.getAmount(), transaction.getSignature());
        }
        return true;
    }

    @Override
    public double balance(String account) {
        process++;
        ServiceProxy counterProxy = new ServiceProxy(process);
            ByteArrayOutputStream out = new ByteArrayOutputStream(1000);
            try {
                new DataOutputStream(out).writeUTF("balance");
                new DataOutputStream(out).writeUTF(account);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
            byte[] reply = counterProxy.invokeUnordered(out.toByteArray()); //magic happens here
            if(reply != null) {
                try {
                    return new DataInputStream(new ByteArrayInputStream(reply)).readDouble();
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            } else {
                System.out.println(", ERROR! Exiting.");
                return 0;
            }
    }

    @Override
    public List<Account> ledger() {
        process++;
        ServiceProxy counterProxy = new ServiceProxy(process);
            ByteArrayOutputStream out = new ByteArrayOutputStream(1000);
            try {
                new DataOutputStream(out).writeUTF("ledger");
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
            byte[] reply = counterProxy.invokeOrdered(out.toByteArray()); //magic happens here
            if(reply != null) {
                try {
                    return JSON.decode(new DataInputStream(new ByteArrayInputStream(reply)).readUTF(),new TypeToken<List<Account>>() {});
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            } else {
                System.out.println(", ERROR! Exiting.");
                return Collections.emptyList();
            }
    }

    @Override
    public String test(){
        process++;
        ServiceProxy counterProxy = new ServiceProxy(process);
            ByteArrayOutputStream out = new ByteArrayOutputStream(1000);
            try {
                new DataOutputStream(out).writeUTF("test");
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
            byte[] reply = counterProxy.invokeOrdered(out.toByteArray());
            if(reply != null) {
                try {
                    return new DataInputStream(new ByteArrayInputStream(reply)).readUTF();
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            } else {
                System.out.println(", ERROR! Exiting.");
                return "ERROR! Exiting.";
            }
    }

    @Override
    public boolean admin(String command, List<String> args,String secret) {
        process++;
        ServiceProxy counterProxy = new ServiceProxy(process);
        ByteArrayOutputStream out = new ByteArrayOutputStream(100);
        System.out.println("Admin command: " + command);
        System.out.println("Secret: " + secret);
        System.out.println("Args: " + args);
        try {
            new DataOutputStream(out).writeUTF("admin");
            new DataOutputStream(out).writeUTF(command);
            new DataOutputStream(out).writeUTF(JSON.encode(args));
            new DataOutputStream(out).writeUTF(secret);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        byte[] reply = counterProxy.invokeUnordered(out.toByteArray()); //magic happens here
        if(reply != null) {
            try {
                return new DataInputStream(new ByteArrayInputStream(reply)).readBoolean();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        } else {
            System.out.println(", ERROR! Exiting.");
            return false;
        }
    }
}
