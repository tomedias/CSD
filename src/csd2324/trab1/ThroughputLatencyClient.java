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

import bftsmart.tom.ServiceProxy;
import bftsmart.tom.util.Storage;
import bftsmart.tom.util.TOMUtil;
import java.io.FileWriter;
import java.nio.ByteBuffer;
import java.security.*;
import java.security.spec.EncodedKeySpec;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.PKCS8EncodedKeySpec;
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
    private static final Random random = new Random();

    public static String privKey = "MD4CAQAwEAYHKoZIzj0CAQYFK4EEAAoEJzAlAgEBBCBnhIob4JXH+WpaNiL72BlbtUMAIBQoM852d+tKFBb7fg==";
    public static String pubKey = "MFYwEAYHKoZIzj0CAQYFK4EEAAoDQgAEavNEKGRcmB7u49alxowlwCi1s24ANOpOQ9UiFBxgqnO/RfOl3BJm0qE2IJgCnvL7XUetwj5C/8MnMWi9ux2aeQ==";


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
        boolean readOnly = Boolean.parseBoolean(args[4]);
        boolean verbose = Boolean.parseBoolean(args[5]);

        BenchClient[] clients = new BenchClient[numThreads];

        for(int i=0; i<numThreads; i++) {
            try {
                Thread.sleep(10);
            } catch (InterruptedException ex) {

                ex.printStackTrace();
            }

            System.out.println("Launching client " + (initId+i));
            clients[i] = new BenchClient(initId+i,numberOfOps,interval,readOnly, verbose);
        }

        ExecutorService exec = Executors.newFixedThreadPool(clients.length);
        Collection<Future<?>> tasks = new LinkedList<>();

        for (BenchClient c : clients) {
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

        System.out.println("All clients done.");
    }

    static class BenchClient extends Thread {

        int id;
        int numberOfOps;
        int interval;
        boolean readOnly;
        boolean verbose;
        Client proxy;
        int rampup = 1000;

        public BenchClient(int id, int numberOfOps, int interval, boolean readOnly, boolean verbose) {
            super("Client "+id);

            this.id = id;
            this.numberOfOps = numberOfOps;
            this.interval = interval;
            this.readOnly = readOnly;
            this.verbose = verbose;
            this.proxy = new Client(String.format("https://localhost:%d/rest",3455+id));

            try {
                PublicKey publicKey = Secure.stringToPublicKey(new BufferedReader(new FileReader(String.format("./tls/rest%d/publickey",id))).readLine());
            } catch (Exception e) {
                e.printStackTrace();
                System.exit(0);
            }

        }

        public void run() {

            System.out.println("Warm up...");

            int req = 0;

            //Inicializar recursos, remover overhead de JIT
            for (int i = 0; i < numberOfOps ; i++, req++) {
                if (verbose) System.out.print("Sending req " + req + "...");

                long last_send_instant = System.nanoTime();

                final int OperationNumber = random.nextInt(Integer.MIN_VALUE,Integer.MAX_VALUE);
                Result<byte[]> result;

                if(readOnly) {
                    result = proxy.balance(map.get("receiver").getId(), OperationNumber);
                }else {
                    result = proxy.transfer(new Transaction(map.get("sender").getId(), map.get("receiver").getId(), 10.0), OperationNumber);
                }

                long latency = System.nanoTime() - last_send_instant;

                try {
                    if (result != null) latencies.put(id + "\t" + System.currentTimeMillis() + "\t" + latency + "\n");
                } catch (InterruptedException ex) {
                    ex.printStackTrace();
                }

                if (verbose) System.out.println(" sent!");

                if (verbose && (req % 1000 == 0)) System.out.println(this.id + " // " + req + " operations sent!");

                try {

                    //sleeps interval ms before sending next request
                    if (interval > 0) {

                        Thread.sleep(interval);
                    }
                    else if (this.rampup > 0) {
                        Thread.sleep(this.rampup);
                    }
                    this.rampup -= 100;

                } catch (InterruptedException ex) {
                    ex.printStackTrace();
                }
            }

            Storage st = new Storage(numberOfOps);

            System.out.println("Executing experiment for " + numberOfOps + " ops");

            for (int i = 0; i < numberOfOps ; i++, req++) {
                if (verbose) System.out.print("Sending req " + req + "...");

                long last_send_instant = System.nanoTime();

                final int OperationNumber = random.nextInt(Integer.MIN_VALUE,Integer.MAX_VALUE);
                Result<byte[]> result;

                if(readOnly) {
                    result = proxy.balance(map.get("receiver").getId(), OperationNumber);
                }else {
                    result = proxy.transfer(new Transaction(map.get("sender").getId(), map.get("receiver").getId(), 10.0), OperationNumber);
                }

                long latency = System.nanoTime() - last_send_instant;

                try {
                    if (result != null) latencies.put(id + "\t" + System.currentTimeMillis() + "\t" + latency + "\n");
                } catch (InterruptedException ex) {
                    ex.printStackTrace();
                }

                if (verbose) System.out.println(this.id + " // sent!");
                st.store(latency);


                try {

                    //sleeps interval ms before sending next request
                    if (interval > 0) {

                        Thread.sleep(interval);
                    }
                    else if (this.rampup > 0) {
                        Thread.sleep(this.rampup);
                    }
                    this.rampup -= 100;

                } catch (InterruptedException ex) {
                    ex.printStackTrace();
                }


                if (verbose && (req % 1000 == 0)) System.out.println(this.id + " // " + req + " operations sent!");
            }

            if(id == initId) {
                System.out.println(this.id + " // Average time for " + numberOfOps + " executions (-10%) = " + st.getAverage(true) / 1000 + " us ");
                System.out.println(this.id + " // Standard deviation for " + numberOfOps + " executions (-10%) = " + st.getDP(true) / 1000 + " us ");
                System.out.println(this.id + " // Average time for " + numberOfOps + " executions (all samples) = " + st.getAverage(false) / 1000 + " us ");
                System.out.println(this.id + " // Standard deviation for " + numberOfOps + " executions (all samples) = " + st.getDP(false) / 1000 + " us ");
                System.out.println(this.id + " // Maximum time for " + numberOfOps + " executions (all samples) = " + st.getMax(false) / 1000 + " us ");
            }
        }
    }
}

