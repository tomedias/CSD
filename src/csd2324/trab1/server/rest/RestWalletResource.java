package csd2324.trab1.server.rest;

import bftsmart.tom.ServiceProxy;
import com.google.gson.reflect.TypeToken;
import csd2324.trab1.api.SignedMessage;
import csd2324.trab1.api.SignedTransaction;
import csd2324.trab1.api.Transaction;
import csd2324.trab1.api.java.Result;
import csd2324.trab1.api.rest.WalletService;
import csd2324.trab1.server.java.JavaWallet;
import csd2324.trab1.utils.JSON;
import csd2324.trab1.utils.Secure;
import jakarta.inject.Singleton;
import java.io.*;
import java.nio.ByteBuffer;
import java.security.*;
import java.util.*;
import java.util.logging.Logger;



@Singleton
public class RestWalletResource implements WalletService {
    private final static Logger Log = Logger.getLogger(RestWalletResource.class.getName());
    private final JavaWallet wallet;
    private final PrivateKey privateKey;
    public RestWalletResource() {
        wallet = new JavaWallet();
        try {
            this.privateKey = Secure.stringToPrivateKey(new BufferedReader(new FileReader(String.format("./tls/rest%d/privatekey",RestWalletServer.SERVER))).readLine());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public byte[] transfer(SignedTransaction transaction, int op_number) {
        try (ServiceProxy counterProxy = new ServiceProxy(op_number)) {
            String command = "transfer";
            String transactionString = JSON.encode(transaction);
            byte[] request = sign(writeCommand(command, transactionString, op_number));
            byte[] reply = counterProxy.invokeOrdered(request);
            if (reply == null) {
                System.out.println(", ERROR! Exiting.");
                return new byte[0];
            }
            List<byte[]> ledger = installSnapshot(reply);
            synchronized (this.wallet){
                executeLedger(ledger);
            }
            byte[] ledgerHash = Secure.hash(JSON.encode(ledger).getBytes());
            SignedMessage<Void> message = new SignedMessage<>(ledgerHash,null,op_number);
            return sign(JSON.encode(message).getBytes());
        } catch (Exception e) {
            e.printStackTrace();
            System.out.println("Error in transfer");
        }
        return new byte[0];
    }

    @Override
    public byte[] atomicTransfer(List<SignedTransaction> signed_transactions, int op_number) {
        try (ServiceProxy counterProxy = new ServiceProxy(op_number)) {
            String command = "atomic";
            String transactions = JSON.encode(signed_transactions);
            byte[] request = sign(writeCommand(command, transactions, op_number));
            byte[] reply = counterProxy.invokeOrdered(request);
            if (reply == null) {
                System.out.println(", ERROR! Exiting.");
                return new byte[0];
            }
            List<byte[]> ledger = installSnapshot(reply);
            synchronized (this.wallet){
               executeLedger(ledger);
            }
            byte[] ledgerHash = Secure.hash(JSON.encode(ledger).getBytes());
            SignedMessage<Void> message = new SignedMessage<>(ledgerHash,null,op_number);
            return sign(JSON.encode(message).getBytes());
        } catch (Exception e) {
            e.printStackTrace();
            System.out.println("Error in atomic transfer");
        }
        return new byte[0];
    }

    @Override
    public byte[] balance(String account, int op_number) {

        try(ServiceProxy counterProxy = new ServiceProxy(op_number)){
            String command = "balance";
            byte[] request = sign(writeCommand(command, account, op_number));
            byte[] reply = counterProxy.invokeUnordered(request);
            if(reply == null) {
                Log.info(", ERROR! Exiting.");
                return new byte[0];
            }
            List<byte[]> ledger = installSnapshot(reply);
            Result<Double> result;
            synchronized (this.wallet){
                executeLedger(ledger);
                result = this.wallet.balance(account);
            }
            if(!result.isOK())
                return new byte[0];
            byte[] ledgerHash = Secure.hash(JSON.encode(ledger).getBytes());
            byte[] operation = ledger.getLast();
            DataInputStream in = new DataInputStream(new ByteArrayInputStream(operation));
            SignedMessage<Double> message = new SignedMessage<>(ledgerHash,result.value(),in.readInt());
            return sign(JSON.encode(message).getBytes());

        } catch (Exception e) {

            e.printStackTrace();
            System.out.println("Error in balance");
        }
        return new byte[0];
    }

    @Override
    public byte[] ledger(int op_number) {
        try(ServiceProxy counterProxy = new ServiceProxy(op_number)) {
            String command = "ledger";
            ByteArrayOutputStream out = new ByteArrayOutputStream(command.length()+ Integer.BYTES);
            new DataOutputStream(out).writeInt(op_number);
            new DataOutputStream(out).writeUTF(command);
            byte[] request = sign(out.toByteArray());
            byte[] reply = counterProxy.invokeUnordered(request);
            if (reply == null) {
                Log.info(", ERROR! Exiting.");
                return new byte[0];
            }
            List<byte[]> ledger = installSnapshot(reply);
            return sign(Secure.hash(getLedgerState(op_number,ledger)));
        } catch (Exception e) {
            e.printStackTrace();
            System.out.println("Error in ledger");
        }
        return new byte[0];
    }

    @Override
    public byte[] admin(Transaction transaction, int op_number) {

        try (ServiceProxy counterProxy = new ServiceProxy(op_number)) {
            String command = "giveme";
            String transactionString = JSON.encode(transaction);
            byte[] request = sign(writeCommand(command, transactionString, op_number));
            byte[] reply = counterProxy.invokeOrdered(request);
            if (reply == null) {
                System.out.println(", ERROR! Exiting.");
                return new byte[0];
            }
            List<byte[]> ledger = installSnapshot(reply);
            synchronized (this.wallet){
                executeLedger(ledger);
            }
            byte[] ledgerHash = Secure.hash(reply);
            SignedMessage<Void> message = new SignedMessage<>(ledgerHash,null,op_number);
            return sign(JSON.encode(message).getBytes());
        } catch (Exception e) {
            e.printStackTrace();
            System.out.println("Error in admin");
        }
        return new byte[0];
    }


    @Override
    public byte[] test(){
        Result<String> result;
        result = this.wallet.test();
        if(!result.isOK()){
            return new byte[0];
        }
        return sign(result.value().getBytes());
    }



    private byte[] sign(byte[] request){
        try {
            byte[] signature = Secure.signData(request, privateKey).getBytes();
            ByteBuffer buffer = ByteBuffer.allocate(request.length + signature.length + (Integer.BYTES * 3));
            buffer.putInt(RestWalletServer.SERVER);
            buffer.putInt(request.length);
            buffer.put(request);
            buffer.putInt(signature.length);
            buffer.put(signature);
            return buffer.array();
        } catch (Exception e) {
            System.out.println("error");
        }
        return new byte[0];
    }

    private synchronized void executeLedger(List<byte[]> ledger){
        synchronized (this.wallet){
            if(this.wallet.getState() > ledger.size()) return;
              for(int i = this.wallet.getState(); i< ledger.size();i++){
                byte[] operation =ledger.get(i);
                try {
                    DataInputStream in = new DataInputStream(new ByteArrayInputStream(operation));
                    in.readInt(); //skip
                    String commandString = in.readUTF();
                    switch(commandString){
                        case "transfer":
                            String signedTransaction = in.readUTF();
                            SignedTransaction transaction = JSON.decode(signedTransaction, SignedTransaction.class);
                            this.wallet.transfer(transaction);
                            break;
                        case "giveme":
                            Transaction admin_transaction = JSON.decode(in.readUTF(),Transaction.class)  ;
                            this.wallet.giveme(admin_transaction);
                            break;
                        case "atomic":
                            String signedTransactions = in.readUTF();
                            List<SignedTransaction> transactions = JSON.decode(signedTransactions, new TypeToken<List<SignedTransaction>>() {});
                            this.wallet.atomicTransfer(transactions);
                            break;
                        default:
                            System.out.println("Read operation, nothing to execute");
                    }
                } catch (IOException e) {
                    System.out.println("Error");
                }
            }
            this.wallet.setState(ledger.size());
        }

    }

    public byte[] getLedgerState(int nonce,List<byte[]> ledger){
        ArrayList<byte[]> stateLedger = new ArrayList<>(ledger.size());
        for (byte[] operation : ledger) {
            try {
                DataInputStream in = new DataInputStream(new ByteArrayInputStream(operation));
                int nonce_read = in.readInt();
                if (nonce_read != nonce) {
                    stateLedger.add(operation);
                } else {
                    stateLedger.add(operation);
                    return getSnapshot(stateLedger);
                }
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
        return new byte[0];
    }

    @SuppressWarnings("unchecked")
    public ArrayList<byte[]> installSnapshot(byte[] state) {
        ByteArrayInputStream bais = new ByteArrayInputStream(state);
        try (ObjectInputStream ois = new ObjectInputStream(bais)) {
            return (ArrayList<byte[]>)ois.readObject();
        } catch (IOException | ClassNotFoundException e) {
            System.out.println("Error");
        }
        return new ArrayList<>();
    }

    public byte[] getSnapshot(ArrayList<byte[]> ledger) {
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        try (ObjectOutputStream oos = new ObjectOutputStream(outputStream)) {
            oos.writeObject(ledger);
            oos.flush();
            return outputStream.toByteArray();
        } catch (IOException e) {
            System.out.println("error");
            return null;
        }
    }

    private byte[] writeCommand(String command, String transaction, int op_number) throws IOException {
        ByteArrayOutputStream out = new ByteArrayOutputStream(command.length() + transaction.length() + Integer.BYTES);
        new DataOutputStream(out).writeInt(op_number);
        new DataOutputStream(out).writeUTF(command);
        new DataOutputStream(out).writeUTF(transaction);
        return out.toByteArray();
    }


}
