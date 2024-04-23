package csd2324.trab1.clients;

import csd2324.trab1.api.java.Wallet;

import java.net.InetAddress;
import java.net.UnknownHostException;

public class Client {
    public static void main(String[] args) throws InterruptedException, UnknownHostException {

//        Thread.sleep(10000);
//        InetAddress address = InetAddress.getByName(args[0]);
//        String ip =address.getHostAddress();
//        String url = "http://"+ip+":3456/rest";
        String url2 = "http://localhost:3456/rest";
        Wallet client = new RestWalletClient(url2);
        System.out.println(client.test().value());
        System.out.println(client.ledger().value());
        System.out.println(client.balance("oi").value());
    }
}
