package csd2324.trab1.server.rest;

import org.glassfish.jersey.server.ResourceConfig;
import java.util.logging.Logger;

public class RestWalletServer extends AbstractRestServer{


    public static int PORT;
    public static int DEVIATE;

    private static final Logger Log = Logger.getLogger(RestWalletServer.class.getName());

    RestWalletServer(String arg){
        super(Log,PORT);
        DEVIATE = Integer.parseInt(arg)*250000000;
    }

    @Override
    protected void registerResources(ResourceConfig config) {
        config.register( RestWalletResource.class );

    }


    public static void main(String[] args) throws Exception {
        if (args.length != 2) {
            System.err.println("Usage: RestWalletServer <server_nr> <port>");
            System.exit(1);
        }
        PORT = Integer.parseInt(args[1]);
        new RestWalletServer(args[0]).start(PORT);
    }
}
