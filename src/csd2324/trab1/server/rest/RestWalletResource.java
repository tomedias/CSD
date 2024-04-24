package csd2324.trab1.server.rest;

import bftsmart.tom.AsynchServiceProxy;
import bftsmart.tom.ServiceProxy;
import bftsmart.tom.core.messages.TOMMessageType;
import com.google.gson.reflect.TypeToken;
import csd2324.trab1.api.rest.WalletService;
import csd2324.trab1.server.java.Account;
import csd2324.trab1.server.java.SignedTransaction;
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

    private int process = 0;
    public RestWalletResource() {

    }

    @Override
    public void transfer(SignedTransaction transaction) {
        process++;
        try (AsynchServiceProxy counterProxy = new AsynchServiceProxy(process)) {
            ByteArrayOutputStream out = new ByteArrayOutputStream(100);
            new DataOutputStream(out).writeUTF("transfer");
            new DataOutputStream(out).writeUTF(JSON.encode(transaction));
            counterProxy.invokeAsynchRequest(out.toByteArray(),new WalletReplyListener(counterProxy),TOMMessageType.ORDERED_REQUEST);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void atomicTransfer(List<SignedTransaction> signed_transactions) {
        //TODO
    }

    @Override
    public double balance(String account) {
        process++;
        try(ServiceProxy counterProxy = new ServiceProxy(process)){
            ByteArrayOutputStream out = new ByteArrayOutputStream(1000);
            new DataOutputStream(out).writeUTF("balance");
            new DataOutputStream(out).writeUTF(account);
            byte[] reply = counterProxy.invokeUnordered(out.toByteArray());
            if(reply != null) {
                return new DataInputStream(new ByteArrayInputStream(reply)).readDouble();
            } else {
                System.out.println(", ERROR! Exiting.");
                return 0;
            }
        }catch (IOException e) {
            throw new RuntimeException(e);
        }

    }

    @Override
    public List<Account> ledger() {
        process++;
        try(ServiceProxy counterProxy = new ServiceProxy(process)) {
            ByteArrayOutputStream out = new ByteArrayOutputStream(1000);
            new DataOutputStream(out).writeUTF("ledger");
            byte[] reply = counterProxy.invokeUnordered(out.toByteArray()); //magic happens here
            if (reply != null) {
                return JSON.decode(new DataInputStream(new ByteArrayInputStream(reply)).readUTF(), new TypeToken<List<Account>>() {});
            } else {
                System.out.println(", ERROR! Exiting.");
                return Collections.emptyList();

            }
        }catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public String test(){
        process++;
        try(ServiceProxy counterProxy = new ServiceProxy(process)) {
            ByteArrayOutputStream out = new ByteArrayOutputStream(1000);
            new DataOutputStream(out).writeUTF("test");
            byte[] reply = counterProxy.invokeUnordered(out.toByteArray());
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
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void admin(Transaction transaction) {
        process++;
        try (AsynchServiceProxy counterProxy = new AsynchServiceProxy(process)) {
            ByteArrayOutputStream out = new ByteArrayOutputStream(100);
            new DataOutputStream(out).writeUTF("giveme");
            new DataOutputStream(out).writeUTF(JSON.encode(transaction));
            counterProxy.invokeAsynchRequest(out.toByteArray(), new WalletReplyListener(counterProxy), TOMMessageType.ORDERED_REQUEST); //magic happens here
        }catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
