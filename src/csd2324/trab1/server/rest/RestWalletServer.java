package csd2324.trab1.server.rest;

import csd2324.trab1.api.java.Wallet;
import org.glassfish.jersey.server.ResourceConfig;

import java.util.logging.Logger;

public class
RestWalletServer extends AbstractRestServer{


    public static final int PORT = 3456;


    private static Logger Log = Logger.getLogger(RestWalletServer.class.getName());

    RestWalletServer(){
        super(Log, Wallet.SERVICE_NAME, PORT);
        Log.info("Test com logger");
    }

    @Override
    protected void registerResources(ResourceConfig config) {
        config.register( RestWalletResource.class );

    }


    public static void main(String[] args) throws Exception {
        new RestWalletServer().start(PORT);
    }
}
