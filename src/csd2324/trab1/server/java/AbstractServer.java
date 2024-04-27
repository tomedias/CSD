package csd2324.trab1.server.java;

import java.util.logging.Logger;


public abstract class AbstractServer {

        protected static String SERVER_BASE_URI = "https://%s:%s%s";
        protected static final String INETADDR_ANY = "0.0.0.0";

        final protected Logger Log;
        final protected String serverURI;

        protected AbstractServer(Logger log, String serverURI) {
            this.Log = log;
            this.serverURI = serverURI;
        }

        abstract protected void start(int port);

        static {
            System.setProperty("java.net.preferIPv4Stack", "true");
            System.setProperty("java.util.logging.SimpleFormatter.format", "%4$s: %5$s");
        }

}
