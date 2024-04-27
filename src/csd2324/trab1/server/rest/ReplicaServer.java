package csd2324.trab1.server.rest;


import bftsmart.tom.MessageContext;
import bftsmart.tom.ServiceReplica;
import bftsmart.tom.server.defaultservices.DefaultSingleRecoverable;
import com.google.gson.reflect.TypeToken;
import csd2324.trab1.utils.JSON;
import csd2324.trab1.utils.Secure;

import java.io.BufferedReader;
import java.io.FileReader;
import java.nio.ByteBuffer;
import java.security.PublicKey;
import java.util.ArrayList;

import java.util.List;
import java.util.logging.Logger;

public class ReplicaServer extends DefaultSingleRecoverable {
    private static Logger Log = Logger.getLogger(ReplicaServer.class.getName());

    private final ServiceReplica replica;
    private List<byte[]> ledger;
    private ArrayList<PublicKey> publicKeys;

    public ReplicaServer(int id) {
        System.setProperty("javax.net.ssl.trustStore", "./tls/truststore");
        System.setProperty("javax.net.ssl.trustStorePassword","changeit");
        this.replica = new ServiceReplica(id, this, this);
        this.ledger = new ArrayList<>();
        this.publicKeys = new ArrayList<>(4);
        for (int i = 1; i <= 4; i++) {
            try {
                publicKeys.add(Secure.stringToPublicKey(new BufferedReader(new FileReader(String.format("./tls/rest%d/publickey",i))).readLine()));
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }


    @Override
    public byte[] appExecuteOrdered(byte[] command, MessageContext msgCtx) {
        try {
            byte[] request = CheckSignature(command);
            ledger.add(request);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        return JSON.encode(ledger).getBytes();
    }

    @Override
    public byte[] appExecuteUnordered(byte[] command, MessageContext msgCtx) {
        try {
            byte[] request = CheckSignature(command);
            ledger.add(request);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        return JSON.encode(ledger).getBytes();
    }

    private byte[] CheckSignature(byte[] command) throws Exception {
        ByteBuffer buffer = ByteBuffer.wrap(command);
        int nr = buffer.getInt();
        int l = buffer.getInt();
        byte[] request = new byte[l];
        buffer.get(request);
        l = buffer.getInt();
        byte[] signature = new byte[l];
        buffer.get(signature);
        String sig = new String(signature);
        PublicKey key = publicKeys.get(nr-1);
        if (!Secure.verifySignature(request, sig, key)) {
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
