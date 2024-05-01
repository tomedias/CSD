package csd2324.trab1;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;

import bftsmart.tom.util.Storage;

import java.io.FileWriter;
import java.security.KeyPair;
import java.util.*;
import java.util.concurrent.*;

import csd2324.trab1.api.Account;
import csd2324.trab1.api.Transaction;
import csd2324.trab1.api.java.Result;
import csd2324.trab1.clients.Client;
import csd2324.trab1.utils.Secure;
import java.security.PublicKey;

public class ThroughputLatencyClient {

    public static int initId = 0;
    static Queue<String> latencies;
    private static final Map<String, Account> map = new HashMap<>();
    private static final List<Client> clients = new ArrayList<>();
    private static final Random random = new Random();
    private static final String admin_id = "MFkwEwYHKoZIzj0CAQYIKoZIzj0DAQcDQgAEaZatK+wN0dHvQOPrFIIOkOoojw8LWCQYhdMO2xw0POF+Ph+mD/TiZG543+2Mplm2hjsQBHBgfrkrVmNbLH8TOQ==";

    @SuppressWarnings("static-access")
    public static void main(String[] args) throws InterruptedException {
        if (args.length < 5) {
            System.out.println("Usage: ... ThroughputLatencyClient <initial client id> <number of clients> <number of operations> <percentage of writes> <verbose?> ");
            System.exit(-1);
        }

        initId = Integer.parseInt(args[0]);
        int numThreads = Integer.parseInt(args[1]);
        int numberOfOps = Integer.parseInt(args[2]);
        int writeRatio = Integer.parseInt(args[3]);
        boolean verbose = Boolean.parseBoolean(args[4]);

        CountDownLatch latch = new CountDownLatch(numberOfOps);

        latencies = new LinkedList<>();
        System.setProperty("javax.net.ssl.trustStore", "tls/truststore");
        System.setProperty("javax.net.ssl.trustStorePassword","changeit");



        BenchClient[] benchClients = new BenchClient[numThreads];


        for(int i=0; i<numThreads; i++) {
            System.out.println("Launching client " + (initId+i));
            benchClients[i] = new BenchClient(initId+i,numberOfOps/numThreads, writeRatio,verbose,latch,numThreads);
            benchClients[i].start();
            Thread.sleep(10);
        }

        latch.await();
        System.out.println("REACHED AWAIT");



        System.out.println("All clients done.");
    }

    static class BenchClient extends Thread {

        private final int id;
        private final int numberOfOps;
        private final int writeRatio;
        private final boolean verbose;
        private final CountDownLatch latch;
        int numThreads;
        Client proxy;
        PublicKey publicKey;

        public BenchClient(int id, int numberOfOps, int writeRatio, boolean verbose,CountDownLatch latch,int numThreads) {
            super("Client "+id);

            this.id = id;
            this.numberOfOps = numberOfOps;
            this.writeRatio = writeRatio;
            this.verbose = verbose;
            this.latch = latch;
            this.numThreads = numThreads;
            this.proxy = new Client(String.format("https://localhost:%d/rest",3455+id));
            clients.add(proxy);

            try {
                this.publicKey = Secure.stringToPublicKey(new BufferedReader(new FileReader(String.format("./tls/rest%d/publickey",(id%4)+1))).readLine());
            } catch (Exception e) {
                e.printStackTrace();
                System.exit(0);
            }

        }

        public void run() {
            System.out.println("Warm up...");

            int req = 0;
            String name = "Client "+id;

            //Initialize endpoints
            map.put(name,proxy.createAccount());
            KeyPair key = proxy.getKeyPair(map.get(name).getId());
            for(int i =0 ; i< clients.size(); i++){
                clients.get(i).addAccount(map.get(name).getId(),key);
            }

            Storage st = new Storage(numberOfOps);

            System.out.println("Executing experiment for " + numberOfOps + " ops");

            Result<byte[]> result;
            final int OperationNumber = random.nextInt(Integer.MIN_VALUE,Integer.MAX_VALUE);
            result = proxy.admin(new Transaction(admin_id,map.get(name).getId(),100),OperationNumber);
            result = proxy.balance(map.get(name).getId(), OperationNumber);

            long t1 = System.nanoTime();

            //reads
            for (int i = 0; i < numberOfOps ; i++, req++) {
                if (verbose) System.out.print("Sending req " + req + "...");
                Random randServer = new Random();

                long last_send_instant = System.nanoTime();

                if(i<numberOfOps*writeRatio /100) {
                    result = clients.get(randServer.nextInt(numThreads)).admin(new Transaction(admin_id,map.get(name).getId(),100),OperationNumber);
                }else {
                    result = clients.get(randServer.nextInt(numThreads)).balance(map.get(name).getId(), OperationNumber);
                }

                long latency = System.nanoTime() - last_send_instant;

                try {
                    if (result != null) latencies.add(id + "\t" + System.currentTimeMillis() + "\t" + latency + "\n");
                } catch (Exception ex) {
                    ex.printStackTrace();
                }

                if (verbose) System.out.println(this.id + " // sent!");
                st.store(latency);

                if (verbose && (req % 1000 == 0)) System.out.println(this.id + " // " + req + " operations sent!");
            }

            long t2 = System.nanoTime() - t1;

                System.out.println(this.id + " // Average time for " + numberOfOps + " executions (-10%) = " + st.getAverage(true) / 1000000 + " ms ");
                System.out.println(this.id + " // Average throughput for " + numberOfOps + " executions (-10%) = " + (numberOfOps)/(st.getAverage(true) / 1000000000) + " /s ");
                System.out.println(this.id + "// Latency = " + (st.getAverage(true) / 1000000)/numberOfOps);
                //System.out.println(this.id + " // Standard deviation for " + numberOfOps + " executions (-10%) = " + st.getDP(true) / 1000 + " us ");
                //System.out.println(this.id + " // Average time for " + numberOfOps + " executions (all samples) = " + st.getAverage(false) / 1000 + " us ");
                //System.out.println(this.id + " // Standard deviation for " + numberOfOps + " executions (all samples) = " + st.getDP(false) / 1000 + " us ");
                //System.out.println(this.id + " // Maximum time for " + numberOfOps + " executions (all samples) = " + st.getMax(false) / 1000 + " us ");
            latch.countDown();
        }
    }
}

