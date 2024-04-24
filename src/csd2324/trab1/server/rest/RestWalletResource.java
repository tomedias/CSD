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
import java.util.concurrent.atomic.AtomicInteger;
import java.util.logging.Logger;



@Singleton
public class RestWalletResource implements WalletService {
    private static Logger Log = Logger.getLogger(RestWalletResource.class.getName());

    private AtomicInteger processID = new AtomicInteger(RestWalletServer.DEVIATE);
    public RestWalletResource() {

    }

    @Override
    public void transfer(SignedTransaction transaction) {
        int process = processID.addAndGet(1);
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
        int process = processID.addAndGet(1);
        try (AsynchServiceProxy counterProxy = new AsynchServiceProxy(process)) {
            ByteArrayOutputStream out = new ByteArrayOutputStream(100);
            new DataOutputStream(out).writeUTF("atomic");
            new DataOutputStream(out).writeUTF(JSON.encode(signed_transactions));
            counterProxy.invokeAsynchRequest(out.toByteArray(),new WalletReplyListener(counterProxy),TOMMessageType.ORDERED_REQUEST);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public double balance(String account) {
        int process = processID.addAndGet(1);
        try(ServiceProxy counterProxy = new ServiceProxy(process)){
            ByteArrayOutputStream out = new ByteArrayOutputStream(1000);
            new DataOutputStream(out).writeUTF("balance");
            new DataOutputStream(out).writeUTF(account);
            byte[] reply = counterProxy.invokeUnordered(out.toByteArray());
            if(reply != null) {
                return new DataInputStream(new ByteArrayInputStream(reply)).readDouble();
            } else {
                Log.info(", ERROR! Exiting.");
                return 0;
            }
        }catch (IOException e) {
            throw new RuntimeException(e);
        }

    }

    @Override
    public List<Account> ledger() {
        int process = processID.addAndGet(1);
        try(ServiceProxy counterProxy = new ServiceProxy(process)) {
            ByteArrayOutputStream out = new ByteArrayOutputStream(1000);
            new DataOutputStream(out).writeUTF("ledger");
            byte[] reply = counterProxy.invokeUnordered(out.toByteArray()); //magic happens here
            if (reply != null) {
                return JSON.decode(new DataInputStream(new ByteArrayInputStream(reply)).readUTF(), new TypeToken<List<Account>>() {});
            } else {
                Log.info(", ERROR! Exiting.");
                return Collections.emptyList();

            }
        }catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public String test(){
        int process = processID.addAndGet(1);
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
        int process = processID.addAndGet(1);
        try (AsynchServiceProxy counterProxy = new AsynchServiceProxy(process)) {
        //try (AsynchServiceProxy counterProxy = new AsynchServiceProxy(process)) {
            ByteArrayOutputStream out = new ByteArrayOutputStream(100);
            new DataOutputStream(out).writeUTF("giveme");
            new DataOutputStream(out).writeUTF(JSON.encode(transaction));
            counterProxy.invokeAsynchRequest(out.toByteArray(), new WalletReplyListener(counterProxy), TOMMessageType.ORDERED_REQUEST); //magic happens here
            //counterProxy.invokeOrdered(out.toByteArray()); //magic happens here
        }catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
