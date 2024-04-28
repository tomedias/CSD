package csd2324.trab1.server.rest;


import bftsmart.tom.MessageContext;
import bftsmart.tom.ServiceReplica;
import bftsmart.tom.server.defaultservices.DefaultSingleRecoverable;
import csd2324.trab1.utils.Secure;
import java.io.*;
import java.security.PublicKey;
import java.util.ArrayList;
import java.util.logging.Logger;

public class ReplicaServer extends DefaultSingleRecoverable {
    private static Logger Log = Logger.getLogger(ReplicaServer.class.getName());

    private ArrayList<byte[]> ledger =  new ArrayList<>();
    private final ArrayList<PublicKey> publicKeys;

    public ReplicaServer(int id) {
        System.setProperty("javax.net.ssl.trustStore", "./tls/truststore");
        System.setProperty("javax.net.ssl.trustStorePassword","changeit");
        new ServiceReplica(id, this, this);
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
            byte[] request = Secure.CheckSignature(command,publicKeys);
            synchronized (ledger){
                ledger.add(request);
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        return getSnapshot();
    }

    @Override
    public byte[] appExecuteUnordered(byte[] command, MessageContext msgCtx) {
        return getSnapshot();
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
        ByteArrayInputStream bais = new ByteArrayInputStream(state);
        try (ObjectInputStream ois = new ObjectInputStream(bais)) {
            ledger = (ArrayList<byte[]>)ois.readObject();
            bais.close();
        } catch (IOException | ClassNotFoundException e) {
            e.printStackTrace();
        }
    }

    @Override
    public byte[] getSnapshot() {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        try (ObjectOutputStream oos = new ObjectOutputStream(baos)) {
            final ArrayList<byte[]> final_ledger = this.ledger;
            synchronized (final_ledger){
                oos.writeObject(final_ledger);
                oos.flush();
                oos.close();
                return baos.toByteArray();
            }
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }
}
