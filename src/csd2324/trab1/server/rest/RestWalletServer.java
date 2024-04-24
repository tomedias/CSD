package csd2324.trab1.server.rest;

import csd2324.trab1.api.java.Wallet;
import org.glassfish.jersey.server.ResourceConfig;

import java.util.logging.Logger;

public class
RestWalletServer extends AbstractRestServer{


    public static final int PORT = 3456;
    public static int DEVIATE;

    private static final Logger Log = Logger.getLogger(RestWalletServer.class.getName());

    RestWalletServer(String agr){
        super(Log,PORT);
        DEVIATE = Integer.parseInt(agr);
    }

    @Override
    protected void registerResources(ResourceConfig config) {
        config.register( RestWalletResource.class );

    }


    public static void main(String[] args) throws Exception {
        new RestWalletServer(args[0]).start(PORT);
    }
}
