package sd2223.trab1.servers.rest;

import org.glassfish.jersey.jdkhttp.JdkHttpServerFactory;
import org.glassfish.jersey.server.ResourceConfig;
import sd2223.trab1.servers.java.Discovery;
import java.net.InetAddress;
import java.net.URI;
import java.util.logging.Logger;
import sd2223.trab1.servers.java.JavaFeeds;

public class RestFeedsServer {
    private static Logger Log = Logger.getLogger(RestFeedsServer.class.getName());

    static {
        System.setProperty("java.net.preferIPv4Stack", "true");
    }

    public static final int PORT = 8084;
    public static final String SERVICE = "feeds";
    private static final String SERVER_URI_FMT = "http://%s:%s/rest";

    public static String DOMAIN;

    public static void main(String[] args) {
        try {
            DOMAIN = args[0];

            JavaFeeds.DOMAIN = DOMAIN;
            ResourceConfig config = new ResourceConfig();
            config.register(RestFeedsResource.class);

            String ip = InetAddress.getLocalHost().getHostAddress();
            String serverURI = String.format(SERVER_URI_FMT, ip, PORT);
            JdkHttpServerFactory.createHttpServer(URI.create(serverURI.replace(ip, "0.0.0.0")), config);

            String serviceName = DOMAIN + ":" + SERVICE;
            // Log.info(String.format("%s Server ready @ %s\n", SERVICE, serverURI));
            // More code can be executed here...
            Discovery.getInstance().announce(serviceName, serverURI);

        } catch (Exception e) {
            // Log.severe(e.getMessage());
        }
    }
}
