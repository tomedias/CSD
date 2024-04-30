package csd2324.trab1;

/**
 Copyright (c) 2007-2013 Alysson Bessani, Eduardo Alchieri, Paulo Sousa, and the authors indicated in the @author tags

 Licensed under the Apache License, Version 2.0 (the "License");
 you may not use this file except in compliance with the License.
 You may obtain a copy of the License at

 http://www.apache.org/licenses/LICENSE-2.0

 Unless required by applicable law or agreed to in writing, software
 distributed under the License is distributed on an "AS IS" BASIS,
 WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 See the License for the specific language governing permissions and
 limitations under the License.
 */

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;

import bftsmart.tom.util.Storage;

import java.io.FileWriter;
import java.security.KeyPair;
import java.util.*;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.LinkedBlockingQueue;

import csd2324.trab1.api.Account;
import csd2324.trab1.api.Transaction;
import csd2324.trab1.api.java.Result;
import csd2324.trab1.clients.Client;
import csd2324.trab1.utils.Secure;
import java.security.PublicKey;

/**
 * Example client that updates a BFT replicated service (a counter).
 *
 */
public class ThroughputLatencyClient {

    public static int initId = 0;
    static LinkedBlockingQueue<String> latencies;
    static Thread writerThread;
    private static final Map<String, Account> map = new HashMap<>();
    private static final List<Client> clients = new ArrayList<>();
    private static final Random random = new Random();
    private static final String admin_id = "MFkwEwYHKoZIzj0CAQYIKoZIzj0DAQcDQgAEaZatK+wN0dHvQOPrFIIOkOoojw8LWCQYhdMO2xw0POF+Ph+mD/TiZG543+2Mplm2hjsQBHBgfrkrVmNbLH8TOQ==";

    @SuppressWarnings("static-access")
    public static void main(String[] args) throws IOException {
        if (args.length < 6) {
            System.out.println("Usage: ... ThroughputLatencyClient <initial client id> <number of clients> <number of operations> <request size> <interval (ms)> <read only?> <verbose?> <nosig | default | ecdsa>");
            System.exit(-1);
        }

        initId = Integer.parseInt(args[0]);
        latencies = new LinkedBlockingQueue<>();
        writerThread = new Thread() {

            public void run() {

                FileWriter f = null;
                try {
                    f = new FileWriter("./latencies_" + initId + ".txt");
                    while (true) {

                        f.write(latencies.take());
                    }

                } catch (IOException | InterruptedException ex) {
                    ex.printStackTrace();
                } finally {
                    try {
                        f.close();
                    } catch (IOException ex) {
                        ex.printStackTrace();
                    }
                }
            }
        };
        writerThread.start();

        int numThreads = Integer.parseInt(args[1]);
        int numberOfOps = Integer.parseInt(args[2]);
        int interval = Integer.parseInt(args[3]);
        int readRatio = Integer.parseInt(args[4]);
        boolean verbose = Boolean.parseBoolean(args[5]);

        final ArrayList<PublicKey> publicKeys = new ArrayList<>(numThreads);
        System.setProperty("javax.net.ssl.trustStore", "tls/truststore");
        System.setProperty("javax.net.ssl.trustStorePassword","changeit");

        BenchClient[] benchClients = new BenchClient[numThreads];

        for(int i=0; i<numThreads; i++) {
            System.out.println("Launching client " + (initId+i));
            benchClients[i] = new BenchClient(initId+i,numberOfOps,interval, readRatio, verbose);
        }

        ExecutorService exec = Executors.newFixedThreadPool(benchClients.length);
        Collection<Future<?>> tasks = new LinkedList<>();

        for (BenchClient c : benchClients) {
            tasks.add(exec.submit(c));
        }

        // wait for tasks completion
        for (Future<?> currTask : tasks) {
            try {
                currTask.get();
            } catch (InterruptedException | ExecutionException ex) {

                ex.printStackTrace();
            }
        }

        exec.shutdown();
        writerThread.interrupt();

        System.out.println("All clients done.");
    }

    static class BenchClient extends Thread {

        int id;
        int numberOfOps;
        int interval;
        int writeRatio;
        boolean verbose;
        Client proxy;
        PublicKey publicKey;

        public BenchClient(int id, int numberOfOps, int interval, int writeRatio, boolean verbose) {
            super("Client "+id);

            this.id = id;
            this.numberOfOps = numberOfOps;
            this.interval = interval;
            this.writeRatio = writeRatio;
            this.verbose = verbose;
            this.proxy = new Client(String.format("https://localhost:%d/rest",3455+id));
            clients.add(proxy);


            try {
                this.publicKey = Secure.stringToPublicKey(new BufferedReader(new FileReader(String.format("./tls/rest%d/publickey",id))).readLine());
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

            //reads
            for (int i = 0; i < numberOfOps ; i++, req++) {
                if (verbose) System.out.print("Sending req " + req + "...");

                Result<byte[]> result;
                final int OperationNumber = random.nextInt(Integer.MIN_VALUE,Integer.MAX_VALUE);
                result = proxy.admin(new Transaction(admin_id,map.get(name).getId(),100),OperationNumber);
                result = proxy.balance(map.get(name).getId(), OperationNumber);

                long last_send_instant = System.nanoTime();

                if(i<numberOfOps*writeRatio /100) {
                    result = proxy.admin(new Transaction(admin_id,map.get(name).getId(),100),OperationNumber);
                }else {
                    result = proxy.balance(map.get(name).getId(), OperationNumber);
                }

                long latency = System.nanoTime() - last_send_instant;

                try {
                    if (result != null) latencies.put(id + "\t" + System.currentTimeMillis() + "\t" + latency + "\n");
                } catch (InterruptedException ex) {
                    ex.printStackTrace();
                }

                if (verbose) System.out.println(this.id + " // sent!");
                st.store(latency);

                if (verbose && (req % 1000 == 0)) System.out.println(this.id + " // " + req + " operations sent!");
            }

            if(id == initId) {
                System.out.println(this.id + " // Average time for " + numberOfOps + " executions (-10%) = " + st.getAverage(true) / 1000 + " us ");
                //System.out.println(this.id + " // Standard deviation for " + numberOfOps + " executions (-10%) = " + st.getDP(true) / 1000 + " us ");
                //System.out.println(this.id + " // Average time for " + numberOfOps + " executions (all samples) = " + st.getAverage(false) / 1000 + " us ");
                //System.out.println(this.id + " // Standard deviation for " + numberOfOps + " executions (all samples) = " + st.getDP(false) / 1000 + " us ");
                //System.out.println(this.id + " // Maximum time for " + numberOfOps + " executions (all samples) = " + st.getMax(false) / 1000 + " us ");
            }
        }
    }
}

