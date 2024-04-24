package csd2324.trab1.server.rest;


import bftsmart.tom.MessageContext;
import bftsmart.tom.ServiceReplica;
import bftsmart.tom.server.defaultservices.DefaultSingleRecoverable;

import com.google.gson.reflect.TypeToken;
import csd2324.trab1.api.java.Result;
import csd2324.trab1.api.java.Wallet;

import csd2324.trab1.server.java.Account;
import csd2324.trab1.server.java.JavaWallet;
import csd2324.trab1.server.java.SignedTransaction;
import csd2324.trab1.server.java.Transaction;
import csd2324.trab1.utils.JSON;
import jakarta.ws.rs.WebApplicationException;

import jakarta.ws.rs.core.Response;

import java.io.*;
import java.util.ArrayList;
import java.util.List;

public class ReplicaServer extends DefaultSingleRecoverable {

    private Wallet wallet;

    public ReplicaServer(int id) {
        new ServiceReplica(id, this, this);
        wallet = new JavaWallet();
    }

    @Override
    public byte[] appExecuteOrdered(byte[] command, MessageContext msgCtx) {
        try {
            DataInputStream in = new DataInputStream(new ByteArrayInputStream(command));
            String commandString = in.readUTF();
            switch(commandString){
                case "transfer":
                    String signedTransaction = in.readUTF();
                    SignedTransaction transaction = JSON.decode(signedTransaction, SignedTransaction.class);
                    fromJavaResult(wallet.transfer(transaction));
                    break;
                case "balance":
                    String account = in.readUTF();
                    System.out.println("Checking balance of account " + account);
                    fromJavaResult(wallet.balance(account));
                    break;
                case "giveme":
                    Transaction admin_transaction = JSON.decode(in.readUTF(),Transaction.class)  ;
                    fromJavaResult(wallet.admin(admin_transaction));
                    break;
                case "atomic":
                    String signedTransactions = in.readUTF();
                    List<SignedTransaction> transactions = JSON.decode(signedTransactions, new TypeToken<List<SignedTransaction>>() {});
                    fromJavaResult(wallet.atomicTransfer(transactions));
                    break;
                default:
                    System.out.println("Unknown command: " + commandString);
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return new byte[0];
    }

    @Override
    public byte[] appExecuteUnordered(byte[] command, MessageContext msgCtx) {
        try {
            DataInputStream in = new DataInputStream(new ByteArrayInputStream(command));
            ByteArrayOutputStream out;
            String commandString = in.readUTF();
            switch(commandString){
                case "balance":
                    String account = in.readUTF();
                    System.out.println("Checking balance of account " + account);
                    double balance = fromJavaResult(wallet.balance(account));
                    out = new ByteArrayOutputStream(10000);
                    new DataOutputStream(out).writeDouble(balance);
                    return out.toByteArray();
                case "test":
                    System.out.println("Test command");
                    String test  = fromJavaResult(wallet.test());
                    out = new ByteArrayOutputStream(10000);
                    new DataOutputStream(out).writeUTF(test);
                    return out.toByteArray();
                case "ledger":
                    System.out.println("Ledger");
                    String ledger = JSON.encode(fromJavaResult(wallet.ledger()));
                    out = new ByteArrayOutputStream(10000);
                    new DataOutputStream(out).writeUTF(ledger);
                    return out.toByteArray();
                default:
                    System.out.println("Unknown command: " + commandString);
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return new byte[0];
    }

    public static void main(String[] args){
        if(args.length < 1) {
            System.out.println("Use: java CounterServer <processId>");
            System.exit(-1);
        }
        new ReplicaServer(Integer.parseInt(args[0]));
    }



    @Override
    public void installSnapshot(byte[] state) {
        try {
            ByteArrayInputStream bis = new ByteArrayInputStream(state);
            ObjectInput in = new ObjectInputStream(bis);
            wallet = JSON.decode(in.readUTF(), Wallet.class);
            in.close();
            bis.close();
        } catch (IOException e) {
            System.err.println("[ERROR] Error deserializing state: "
                    + e.getMessage());
        }
    }

    @Override
    public byte[] getSnapshot() {
        try {
            ByteArrayOutputStream bos = new ByteArrayOutputStream();
            ObjectOutput out = new ObjectOutputStream(bos);
            out.writeUTF(JSON.encode(wallet));
            out.flush();
            bos.flush();
            out.close();
            bos.close();
            return bos.toByteArray();
        } catch (IOException ioe) {
            System.err.println("[ERROR] Error serializing state: "
                    + ioe.getMessage());
            return "ERROR".getBytes();
        }
    }

    /**
     * Given a Result<T>, either returns the value, or throws the JAX-WS Exception
     * matching the error code...
     */
    protected <T> T fromJavaResult(Result<T> result) {
        if (result.isOK())
            return result.value();
        if( result.error() == Result.ErrorCode.REDIRECTED && result.errorValue() != null )
            return result.errorValue();
        throw new WebApplicationException(statusCodeFrom(result));
    }

    /**
     * Translates a Result<T> to a HTTP Status code
     */
    protected static Response.Status statusCodeFrom(Result<?> result) {
        return switch (result.error()) {
            case CONFLICT -> Response.Status.CONFLICT;
            case NOT_FOUND -> Response.Status.NOT_FOUND;
            case FORBIDDEN -> Response.Status.FORBIDDEN;
            case TIMEOUT, BAD_REQUEST -> Response.Status.BAD_REQUEST;
            case NOT_IMPLEMENTED -> Response.Status.NOT_IMPLEMENTED;
            case REDIRECTED -> result.errorValue() == null ? Response.Status.NO_CONTENT : Response.Status.OK;
            case OK -> result.value() == null ? Response.Status.NO_CONTENT : Response.Status.OK;
            default -> Response.Status.INTERNAL_SERVER_ERROR;
        };
    }
}
