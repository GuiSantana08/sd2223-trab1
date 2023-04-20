package sd2223.trab1.servers.soap;

import java.net.InetAddress;
import java.util.logging.Level;
import java.util.logging.Logger;

import jakarta.xml.ws.Endpoint;

public class SoapUsersServer {

    public static final int PORT = 8082;
    public static final String SERVICE_NAME = "users";
    public static String SERVER_BASE_URI = "http://%s:%s/soap";

    private static Logger Log = Logger.getLogger(SoapUsersServer.class.getName());

    public static void main(String[] args) throws Exception {

        // Log.setLevel(Level.INFO);

        String ip = InetAddress.getLocalHost().getHostAddress();
        String serverURI = String.format(SERVER_BASE_URI, ip, PORT);

        Endpoint.publish(serverURI.replace(ip, "0.0.0.0"), new SoapUsersWebService());

        // Log.info(String.format("%s Soap Server ready @ %s\n", SERVICE_NAME,
        // serverURI));
    }
}
