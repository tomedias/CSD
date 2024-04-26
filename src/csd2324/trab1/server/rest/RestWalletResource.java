package csd2324.trab1.server.rest;

import bftsmart.tom.AsynchServiceProxy;
import bftsmart.tom.ServiceProxy;
import bftsmart.tom.core.messages.TOMMessageType;
import bftsmart.tom.util.TOMUtil;
import com.google.gson.reflect.TypeToken;
import csd2324.trab1.api.SignedMessage;
import csd2324.trab1.api.SignedTransaction;
import csd2324.trab1.api.Transaction;
import csd2324.trab1.api.rest.WalletService;
import csd2324.trab1.server.java.JavaWallet;
import csd2324.trab1.utils.JSON;
import jakarta.inject.Singleton;
import java.io.*;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.Signature;
import java.security.SignatureException;
import java.util.*;
import java.util.concurrent.CountDownLatch;
import java.util.logging.Logger;



@Singleton
public class RestWalletResource implements WalletService {
    private final static Logger Log = Logger.getLogger(RestWalletResource.class.getName());
    private final JavaWallet wallet;
    private final Random random = new Random();
    public RestWalletResource() {
        wallet = new JavaWallet();
    }

    @Override
    public byte[] transfer(SignedTransaction transaction) {
        int process = random.nextInt(Integer.MIN_VALUE,Integer.MAX_VALUE);
        try (AsynchServiceProxy serviceProxy = new AsynchServiceProxy(process)) {
            String command = "transfer";
            String transactionString = JSON.encode(transaction);
            ByteArrayOutputStream out = new ByteArrayOutputStream(command.length() + transactionString.length());
            new DataOutputStream(out).writeUTF(command);
            new DataOutputStream(out).writeUTF(transactionString);
            byte[] request = getSignedRequest(out.toByteArray(), serviceProxy);
            final CountDownLatch latch = new CountDownLatch(2);
            SignedMessage message = new SignedMessage(request);
            serviceProxy.invokeAsynchRequest(request, new WalletReplyListener(serviceProxy,latch,message) ,TOMMessageType.UNORDERED_REQUEST);
            latch.await();
            return JSON.encode(message).getBytes(StandardCharsets.UTF_8);
        } catch (IOException | InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public byte[] atomicTransfer(List<SignedTransaction> signed_transactions) {
        int process = random.nextInt(Integer.MIN_VALUE,Integer.MAX_VALUE);
        try (AsynchServiceProxy serviceProxy = new AsynchServiceProxy(process)) {
            String command = "atomic";
            String transactions = JSON.encode(signed_transactions);
            ByteArrayOutputStream out = new ByteArrayOutputStream(command.length() + transactions.length());
            new DataOutputStream(out).writeUTF(command);
            new DataOutputStream(out).writeUTF(transactions);
            byte[] request = getSignedRequest(out.toByteArray(),serviceProxy);
            final CountDownLatch latch = new CountDownLatch(2);
            SignedMessage message = new SignedMessage(request);
            serviceProxy.invokeAsynchRequest(request, new WalletReplyListener(serviceProxy,latch,message) ,TOMMessageType.UNORDERED_REQUEST);
            latch.await();
            return JSON.encode(message).getBytes(StandardCharsets.UTF_8);
        } catch (IOException | InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public byte[] balance(String account) {
        int process = random.nextInt(Integer.MIN_VALUE,Integer.MAX_VALUE);
        try(AsynchServiceProxy serviceProxy = new AsynchServiceProxy(process)){
            String command = "balance";
            ByteArrayOutputStream out = new ByteArrayOutputStream(command.length() + account.length());
            new DataOutputStream(out).writeUTF(command);
            new DataOutputStream(out).writeUTF(account);
            byte[] request = getSignedRequest(out.toByteArray(),serviceProxy);
            final CountDownLatch latch = new CountDownLatch(2);
            SignedMessage message = new SignedMessage(request);
            serviceProxy.invokeAsynchRequest(request, new WalletReplyListener(serviceProxy,latch,message) ,TOMMessageType.UNORDERED_REQUEST);
            latch.await();
            return JSON.encode(message).getBytes(StandardCharsets.UTF_8);
        }catch (IOException | InterruptedException e) {
            throw new RuntimeException(e);
        }

    }

    @Override
    public byte[] ledger() {
        int process = random.nextInt(Integer.MIN_VALUE,Integer.MAX_VALUE);
        try(AsynchServiceProxy serviceProxy = new AsynchServiceProxy(process)) {
            String command = "ledger";
            ByteArrayOutputStream out = new ByteArrayOutputStream(command.length());
            new DataOutputStream(out).writeUTF(command);
            byte[] request = getSignedRequest(out.toByteArray(),serviceProxy);
            final CountDownLatch latch = new CountDownLatch(2);
            SignedMessage message = new SignedMessage(request);
            serviceProxy.invokeAsynchRequest(request, new WalletReplyListener(serviceProxy,latch,message) ,TOMMessageType.UNORDERED_REQUEST);
            latch.await();
            return JSON.encode(message).getBytes(StandardCharsets.UTF_8);
        }catch (IOException | InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public byte[] test(){
        int process = random.nextInt(Integer.MIN_VALUE,Integer.MAX_VALUE);
        try(AsynchServiceProxy serviceProxy = new AsynchServiceProxy(process)) {
            String command = "test";
            ByteArrayOutputStream out = new ByteArrayOutputStream(command.length());
            new DataOutputStream(out).writeUTF(command);
            byte[] request = getSignedRequest(out.toByteArray(),serviceProxy);
            final CountDownLatch latch = new CountDownLatch(2);
            SignedMessage message = new SignedMessage(request);
            serviceProxy.invokeAsynchRequest(request, new WalletReplyListener(serviceProxy,latch,message) ,TOMMessageType.UNORDERED_REQUEST);
            latch.await();
            return JSON.encode(message).getBytes(StandardCharsets.UTF_8);

        } catch (IOException | InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public byte[] admin(Transaction transaction) {
        int process = random.nextInt(Integer.MIN_VALUE,Integer.MAX_VALUE);
        try (final AsynchServiceProxy serviceProxy = new AsynchServiceProxy(process)) {
            String command = "giveme";
            String transactionString = JSON.encode(transaction);
            ByteArrayOutputStream out = new ByteArrayOutputStream(command.length() + transactionString.length());
            new DataOutputStream(out).writeUTF(command);
            new DataOutputStream(out).writeUTF(transactionString);
            byte[] request = getSignedRequest(out.toByteArray(), serviceProxy);
            final CountDownLatch latch = new CountDownLatch(2);
            SignedMessage message = new SignedMessage(request);
            serviceProxy.invokeAsynchRequest(request, new WalletReplyListener(serviceProxy, latch,message), TOMMessageType.ORDERED_REQUEST);
            latch.await();
            return JSON.encode(message).getBytes(StandardCharsets.UTF_8);
        }catch (IOException | InterruptedException e) {
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

    private void executeLedger(List<byte[]> ledger){

        for(int i = wallet.getState(); i< ledger.size();i++){
            byte[] operation =ledger.get(i);
            try {
                DataInputStream in = new DataInputStream(new ByteArrayInputStream(operation));
                String commandString = in.readUTF();
                switch(commandString){
                    case "transfer":
                        String signedTransaction = in.readUTF();
                        SignedTransaction transaction = JSON.decode(signedTransaction, SignedTransaction.class);
                        wallet.transfer(transaction);
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
        wallet.setState(ledger.size());
    }


}
