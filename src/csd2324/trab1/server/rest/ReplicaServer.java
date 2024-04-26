package csd2324.trab1.server.rest;


import bftsmart.tom.MessageContext;
import bftsmart.tom.ServiceProxy;
import bftsmart.tom.ServiceReplica;
import bftsmart.tom.server.defaultservices.DefaultSingleRecoverable;
import bftsmart.tom.util.TOMUtil;
import com.google.gson.reflect.TypeToken;
import csd2324.trab1.utils.JSON;

import java.io.*;
import java.nio.ByteBuffer;
import java.security.*;
import java.security.spec.EncodedKeySpec;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.PKCS8EncodedKeySpec;
import java.util.ArrayList;

import java.util.List;
import java.util.logging.Logger;

public class ReplicaServer extends DefaultSingleRecoverable {
    private static Logger Log = Logger.getLogger(ReplicaServer.class.getName());
    private  String privKey = "";
    private final ServiceReplica replica;
    private List<byte[]> ledger;


    public ReplicaServer(int id) {
        this.replica = new ServiceReplica(id, this, this);
        this.ledger = new ArrayList<>();
        try {
            this.privKey = new BufferedReader(new FileReader("./config/privatekey"+id)).readLine();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }


    @Override
    public byte[] appExecuteOrdered(byte[] command, MessageContext msgCtx) {
        try {
            byte[] request = CheckSignature(command);
            ledger.add(request);
            byte[] reply = JSON.encode(ledger).getBytes();
            return getSignedRequest(reply);
        } catch (NoSuchAlgorithmException | InvalidKeyException | SignatureException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public byte[] appExecuteUnordered(byte[] command, MessageContext msgCtx) {
        Log.info("Unordered execution is not supported");
        try {
            byte[] request = CheckSignature(command);
            ledger.add(request);
            byte[] reply = JSON.encode(ledger).getBytes();
            return getSignedRequest(reply);
        } catch (NoSuchAlgorithmException | InvalidKeyException | SignatureException e) {
            throw new RuntimeException(e);
        }
    }

    private byte[] getSignedRequest(byte[] request){
        try {
            Signature eng;
            eng = Signature.getInstance("SHA256withECDSA", "BC");

            KeyFactory keyFactory = KeyFactory.getInstance("EC");
            EncodedKeySpec privateKeySpec = new PKCS8EncodedKeySpec(org.apache.commons.codec.binary.Base64.decodeBase64(privKey));
            PrivateKey privateKey = keyFactory.generatePrivate(privateKeySpec);
            eng.initSign(privateKey);
            byte[] signature = eng.sign();
            ByteBuffer buffer = ByteBuffer.allocate(request.length + signature.length + (Integer.BYTES * 2));
            buffer.putInt(this.replica.getId());
            buffer.putInt(request.length);
            buffer.put(request);
            buffer.putInt(signature.length);
            buffer.put(signature);
            return buffer.array();
        } catch (SignatureException | InvalidKeyException | NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        } catch (InvalidKeySpecException e) {
            throw new RuntimeException(e);
        } catch (NoSuchProviderException e) {
            throw new RuntimeException(e);
        }
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
