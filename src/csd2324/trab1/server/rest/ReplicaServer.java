package csd2324.trab1.server.rest;


import bftsmart.tom.MessageContext;
import bftsmart.tom.ServiceReplica;
import bftsmart.tom.server.defaultservices.DefaultSingleRecoverable;
import bftsmart.tom.util.TOMUtil;
import com.google.gson.reflect.TypeToken;
import csd2324.trab1.utils.JSON;

import java.io.*;
import java.nio.ByteBuffer;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.Signature;
import java.security.SignatureException;
import java.util.ArrayList;

import java.util.List;
import java.util.logging.Logger;

public class ReplicaServer extends DefaultSingleRecoverable {
    private static Logger Log = Logger.getLogger(ReplicaServer.class.getName());

    private final ServiceReplica replica;
    private List<byte[]> ledger;


    public ReplicaServer(int id) {
        this.replica = new ServiceReplica(id, this, this);
        this.ledger = new ArrayList<>();
    }


    @Override
    public byte[] appExecuteOrdered(byte[] command, MessageContext msgCtx) {
        try {
            byte[] request = CheckSignature(command);
            ledger.add(request);
        } catch (NoSuchAlgorithmException | InvalidKeyException | SignatureException e) {
            throw new RuntimeException(e);
        }
        return new byte[0];


    }

    @Override
    public byte[] appExecuteUnordered(byte[] command, MessageContext msgCtx) {
        Log.info("Unordered execution is not supported");
        try {
            byte[] request = CheckSignature(command);
            ledger.add(request);
        } catch (NoSuchAlgorithmException | InvalidKeyException | SignatureException e) {
            throw new RuntimeException(e);
        }
        String answer = JSON.encode(ledger);
        return answer.getBytes();
    }

    private byte[] CheckSignature(byte[] command) throws NoSuchAlgorithmException, InvalidKeyException, SignatureException {
        ByteBuffer buffer = ByteBuffer.wrap(command);
        int l = buffer.getInt();
        byte[] request = new byte[l];
        buffer.get(request);
        l = buffer.getInt();
        byte[] signature = new byte[l];
        buffer.get(signature);
        Signature eng;
        eng = TOMUtil.getSigEngine();
        eng.initVerify(replica.getReplicaContext().getStaticConfiguration().getPublicKey());
        eng.update(request);
        if (!eng.verify(signature)) {
            System.out.println("Client sent invalid signature!");
            System.exit(0);
        }


        return request;
    }

    public static void main(String[] args){
        if(args.length < 1) {
            System.out.println("Use: java CounterServer <processId>");
            System.exit(-1);
        }
        new ReplicaServer(Integer.parseInt(args[0]));
    }



    @SuppressWarnings("unchecked")
    @Override
    public void installSnapshot(byte[] state) {
       ledger = JSON.decode(new String(state), new TypeToken<List<byte[]>>() {});
    }

    @Override
    public byte[] getSnapshot() {
        return JSON.encode(ledger).getBytes();
    }
}
