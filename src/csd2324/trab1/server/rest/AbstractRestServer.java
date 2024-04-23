package csd2324.trab1.server.rest;

import csd2324.trab1.server.java.AbstractServer;
import csd2324.trab1.utils.IP;
import org.glassfish.jersey.server.ResourceConfig;
import org.glassfish.jersey.jdkhttp.JdkHttpServerFactory;
import java.util.logging.Logger;
import java.net.URI;

public abstract class AbstractRestServer extends AbstractServer {

    private static final String REST_CTX = "/rest";
    protected AbstractRestServer(Logger log, String service, int port) {
        super(log, String.format(SERVER_BASE_URI, IP.hostName(), port, REST_CTX));
    }

    protected void start(int port) {
        try{
            ResourceConfig config = new ResourceConfig();

            registerResources( config );
            JdkHttpServerFactory.createHttpServer( URI.create(serverURI.replace(IP.hostAddress(), INETADDR_ANY)), config);
            Log.info(String.format("Server ready @ %s\n",serverURI));
            Log.info("Test\n");
            Log.info(String.format("Server ip %s\n", IP.hostName()));
        }catch (Exception e) {
            Log.severe(e.getMessage());
        }

    }

    protected abstract void registerResources(ResourceConfig config);
}
