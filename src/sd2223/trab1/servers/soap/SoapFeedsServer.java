package sd2223.trab1.servers.soap;

import jakarta.xml.ws.Endpoint;
import sd2223.trab1.servers.java.Discovery;
import sd2223.trab1.servers.java.JavaFeeds;

import java.net.InetAddress;
import java.util.logging.Level;
import java.util.logging.Logger;

public class SoapFeedsServer {

    public static final int PORT = 8081;
    public static final String SERVICE_NAME = "feeds";
    public static String SERVER_BASE_URI = "http://%s:%s/soap";

    public static String DOMAIN;

    private static Logger Log = Logger.getLogger(SoapFeedsServer.class.getName());

    public static void main(String[] args) throws Exception {
        DOMAIN = args[0];
        JavaFeeds.DOMAIN = DOMAIN;
        //Log.info("DOMAIN  ARG " + args[0]);
        //Log.info("DOMAIN  " + DOMAIN);
        // Log.setLevel(Level.INFO);

        String ip = InetAddress.getLocalHost().getHostAddress();
        String serverURI = String.format(SERVER_BASE_URI, ip, PORT);

        Endpoint.publish(serverURI.replace(ip, "0.0.0.0"), new SoapFeedsWebService());
        String serviceName = DOMAIN + ":" + SERVICE_NAME;
        Log.info(String.format("%s Soap Server ready @ %s\n", serviceName, serverURI));

        Discovery.getInstance().announce(serviceName, serverURI);
    }
}
