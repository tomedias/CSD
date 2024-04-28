package csd2324.trab1.server.rest;

import org.glassfish.jersey.server.ResourceConfig;
import java.util.logging.Logger;

public class RestWalletServer extends AbstractRestServer{


    public static int PORT;
    public static int SERVER;
    private static final Logger Log = Logger.getLogger(RestWalletServer.class.getName());

    RestWalletServer(){
        super(Log,PORT);
        System.setProperty("javax.net.ssl.keyStore", "./tls/rest"+SERVER+"/keystore");
        System.setProperty("javax.net.ssl.keyStorePassword", "changeit");
    }

    @Override
    protected void registerResources(ResourceConfig config) {
        config.register( RestWalletResource.class);
    }


    public static void main(String[] args) {
        if (args.length != 2) {
            System.err.println("Usage: RestWalletServer <server_nr> <port>");
            System.exit(1);
        }
        SERVER = Integer.parseInt(args[0]);
        PORT = Integer.parseInt(args[1]);
        new RestWalletServer().start(PORT);
    }
}
