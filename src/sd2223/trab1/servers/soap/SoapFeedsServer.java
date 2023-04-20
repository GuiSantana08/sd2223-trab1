package sd2223.trab1.servers.soap;

import jakarta.xml.ws.Endpoint;

import java.net.InetAddress;
import java.util.logging.Level;
import java.util.logging.Logger;

public class SoapFeedsServer {

    public static final int PORT = 8081;
    public static final String SERVICE_NAME = "feeds";
    public static String SERVER_BASE_URI = "http://%s:%s/soap";

    private static Logger Log = Logger.getLogger(SoapFeedsServer.class.getName());

    public static void main(String[] args) throws Exception {

        // Log.setLevel(Level.INFO);

        String ip = InetAddress.getLocalHost().getHostAddress();
        String serverURI = String.format(SERVER_BASE_URI, ip, PORT);

        Endpoint.publish(serverURI.replace(ip, "0.0.0.0"), new SoapUsersWebService());

        // Log.info(String.format("%s Soap Server ready @ %s\n", SERVICE_NAME,
        // serverURI));
    }
}
