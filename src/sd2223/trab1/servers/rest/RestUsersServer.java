package sd2223.trab1.servers.rest;

import org.glassfish.jersey.jdkhttp.JdkHttpServerFactory;
import org.glassfish.jersey.server.ResourceConfig;
import sd2223.trab1.servers.java.Discovery;

import java.net.InetAddress;
import java.net.URI;
import java.util.logging.Logger;

public class RestUsersServer {

    private static Logger Log = Logger.getLogger(RestUsersServer.class.getName());

    static {
        System.setProperty("java.net.preferIPv4Stack", "true");
    }

    public static final int PORT = 8083;
    public static final String SERVICE = "users";
    private static final String SERVER_URI_FMT = "http://%s:%s/rest";

    public static String DOMAIN;

    public static void main(String[] args) {
        try {
            DOMAIN = args[0];
            String msgID = args[1];
            ResourceConfig config = new ResourceConfig();
            config.register(RestUsersResource.class);
            // config.register(CustomLoggingFilter.class);

            String ip = InetAddress.getLocalHost().getHostAddress();
            String serverURI = String.format(SERVER_URI_FMT, ip, PORT);
            JdkHttpServerFactory.createHttpServer(URI.create(serverURI.replace(ip, "0.0.0.0")), config);

            String serviceName = DOMAIN + ":" + SERVICE;
            // Log.info(String.format("%s Server ready @ %s\n", serviceName, serverURI));
            // More code can be executed here...
            Discovery.getInstance().announce(serviceName, serverURI);

        } catch (Exception e) {
            // Log.severe(e.getMessage());
        }
    }
}
