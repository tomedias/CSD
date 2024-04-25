package csd2324.trab1.server.rest;

import bftsmart.tom.AsynchServiceProxy;
import bftsmart.tom.ServiceProxy;
import bftsmart.tom.core.messages.TOMMessageType;
import bftsmart.tom.util.TOMUtil;
import com.google.gson.reflect.TypeToken;

import csd2324.trab1.api.Account;
import csd2324.trab1.api.SignedTransaction;
import csd2324.trab1.api.Transaction;
import csd2324.trab1.api.java.Wallet;
import csd2324.trab1.api.rest.WalletService;
import csd2324.trab1.server.java.JavaWallet;
import csd2324.trab1.utils.JSON;
import jakarta.inject.Singleton;
import java.io.*;
import java.nio.ByteBuffer;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.Signature;
import java.security.SignatureException;
import java.util.*;
import java.util.logging.Logger;



@Singleton
public class RestWalletResource implements WalletService {
    private static Logger Log = Logger.getLogger(RestWalletResource.class.getName());
    Random random = new Random();
    public RestWalletResource() {

    }

    @Override
    public void transfer(SignedTransaction transaction) {
        int process = random.nextInt(Integer.MIN_VALUE,Integer.MAX_VALUE);
        try (AsynchServiceProxy counterProxy = new AsynchServiceProxy(process)) {
            String command = "transfer";
            String transactionString = JSON.encode(transaction);
            ByteArrayOutputStream out = new ByteArrayOutputStream(command.length() + transactionString.length());
            new DataOutputStream(out).writeUTF(command);
            new DataOutputStream(out).writeUTF(transactionString);
            byte[] request = getSignedRequest(out.toByteArray(), counterProxy);
            counterProxy.invokeAsynchRequest(request,new WalletReplyListener(counterProxy),TOMMessageType.ORDERED_REQUEST);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void atomicTransfer(List<SignedTransaction> signed_transactions) {
        int process = random.nextInt(Integer.MIN_VALUE,Integer.MAX_VALUE);
        try (AsynchServiceProxy counterProxy = new AsynchServiceProxy(process)) {
            String command = "atomic";
            String transactions = JSON.encode(signed_transactions);
            ByteArrayOutputStream out = new ByteArrayOutputStream(command.length() + transactions.length());
            new DataOutputStream(out).writeUTF(command);
            new DataOutputStream(out).writeUTF(transactions);
            byte[] request = getSignedRequest(out.toByteArray(),counterProxy);
            counterProxy.invokeAsynchRequest(request,new WalletReplyListener(counterProxy),TOMMessageType.ORDERED_REQUEST);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public double balance(String account) {
        int process = random.nextInt(Integer.MIN_VALUE,Integer.MAX_VALUE);
        try(ServiceProxy counterProxy = new ServiceProxy(process)){
            String command = "balance";
            ByteArrayOutputStream out = new ByteArrayOutputStream(command.length() + account.length());
            new DataOutputStream(out).writeUTF(command);
            new DataOutputStream(out).writeUTF(account);
            byte[] request = getSignedRequest(out.toByteArray(),counterProxy);
            byte[] reply = counterProxy.invokeUnordered(request);
            if(reply == null) {
                Log.info(", ERROR! Exiting.");
                return 0;
            }
            List<byte[]> ledger = JSON.decode(new DataInputStream(new ByteArrayInputStream(reply)).readUTF(), new TypeToken<List<byte[]>>() {});
            Wallet currentState = executeLedger(ledger);
            return currentState.balance(account).isOK() ? currentState.balance(account).value() : 0;
        }catch (IOException e) {
            throw new RuntimeException(e);
        }

    }

    @Override
    public List<Account> ledger() {
        int process = random.nextInt(Integer.MIN_VALUE,Integer.MAX_VALUE);
        try(ServiceProxy counterProxy = new ServiceProxy(process)) {
            String command = "ledger";
            ByteArrayOutputStream out = new ByteArrayOutputStream(command.length());
            new DataOutputStream(out).writeUTF(command);
            byte[] request = getSignedRequest(out.toByteArray(),counterProxy);
            byte[] reply = counterProxy.invokeUnordered(request);
            if (reply == null) {
                Log.info(", ERROR! Exiting.");
                return Collections.emptyList();
            }
            List<byte[]> ledger = JSON.decode(new DataInputStream(new ByteArrayInputStream(reply)).readUTF(), new TypeToken<List<byte[]>>() {});
            Wallet currentState = executeLedger(ledger);
            return currentState.ledger().isOK() ? currentState.ledger().value() : new LinkedList<>();
        }catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public String test(){
        Log.info("got here");
        int process = random.nextInt(Integer.MIN_VALUE,Integer.MAX_VALUE);
        try(ServiceProxy counterProxy = new ServiceProxy(process)) {
            String command = "test";
            ByteArrayOutputStream out = new ByteArrayOutputStream(command.length());
            new DataOutputStream(out).writeUTF(command);
            byte[] request = getSignedRequest(out.toByteArray(),counterProxy);
            byte[] reply = counterProxy.invokeUnordered(request);
            if (reply == null) {
                Log.info(", ERROR! Exiting.");
                return "";
            }
            List<byte[]> ledger = JSON.decode(new DataInputStream(new ByteArrayInputStream(reply)).readUTF(), new TypeToken<List<byte[]>>() {});
            Wallet currentState = executeLedger(ledger);
            return currentState.test().isOK() ? currentState.test().value() : "";
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void admin(Transaction transaction) {
        int process = random.nextInt(Integer.MIN_VALUE,Integer.MAX_VALUE);
        System.out.println(process);
        try (AsynchServiceProxy counterProxy = new AsynchServiceProxy(process)) {
            String command = "giveme";
            String transactionString = JSON.encode(transaction);
            ByteArrayOutputStream out = new ByteArrayOutputStream(command.length() + transactionString.length());
            new DataOutputStream(out).writeUTF(command);
            new DataOutputStream(out).writeUTF(transactionString);
            byte[] request = getSignedRequest(out.toByteArray(), counterProxy);
            counterProxy.invokeAsynchRequest(request, new WalletReplyListener(counterProxy), TOMMessageType.ORDERED_REQUEST);
        }catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private byte[] getSignedRequest(byte[] request,ServiceProxy proxy){
        try {
            Signature eng;
            eng = TOMUtil.getSigEngine();
            eng.initSign(proxy.getViewManager().getStaticConf().getPrivateKey());
            eng.update(request);
            byte[] signature = eng.sign();
            ByteBuffer buffer = ByteBuffer.allocate(request.length + signature.length + (Integer.BYTES * 2));
            buffer.putInt(request.length);
            buffer.put(request);
            buffer.putInt(signature.length);
            buffer.put(signature);
            return buffer.array();
        } catch (SignatureException | InvalidKeyException | NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        }
    }

    private Wallet executeLedger(List<byte[]> ledger){
        Wallet wallet = new JavaWallet();
        for (byte[] command : ledger){
            try {
                DataInputStream in = new DataInputStream(new ByteArrayInputStream(command));
                String commandString = in.readUTF();
                switch(commandString){
                    case "transfer":
                        String signedTransaction = in.readUTF();
                        SignedTransaction transaction = JSON.decode(signedTransaction, SignedTransaction.class);
                        wallet.transfer(transaction);
                        break;
                    case "balance":
                        String account = in.readUTF();
                        System.out.println("Checking balance of account " + account);
                        wallet.balance(account);
                        break;
                    case "giveme":
                        Transaction admin_transaction = JSON.decode(in.readUTF(),Transaction.class)  ;
                        wallet.giveme(admin_transaction);
                        break;
                    case "atomic":
                        String signedTransactions = in.readUTF();
                        List<SignedTransaction> transactions = JSON.decode(signedTransactions, new TypeToken<List<SignedTransaction>>() {});
                        wallet.atomicTransfer(transactions);
                        break;
                    default:
                        System.out.println("Read operation, nothing to execute");
                    }
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
        return wallet;
    }


}
