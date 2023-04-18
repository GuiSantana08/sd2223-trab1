package sd2223.trab1.clients;

import sd2223.trab1.api.java.Feeds;
import sd2223.trab1.api.java.Users;
import sd2223.trab1.clients.rest.RestFeedsClient;
import sd2223.trab1.clients.rest.RestUsersClient;
import sd2223.trab1.clients.soap.SoapFeedsClient;
import sd2223.trab1.clients.soap.SoapUsersClient;

public class UsersClientFactory {

    private static final String REST = "/rest";

    private static final String SOAP = "/soap";

    public static Users getUserClient(java.net.URI serverURI) {
        var uriString = serverURI.toString();

        if (uriString.endsWith(REST))
            return new RestUsersClient(serverURI);
        else if (uriString.endsWith(SOAP))
            return new SoapUsersClient(serverURI);
        else
            throw new RuntimeException("Unknown service type..." + uriString);
    }

    public static Feeds getFeedsClient(java.net.URI serverURI) {
        var uriString = serverURI.toString();

        if (uriString.endsWith(REST))
            return new RestFeedsClient(serverURI);
        else if (uriString.endsWith(SOAP))
            return new SoapFeedsClient(serverURI);
        else
            throw new RuntimeException("Unknown service type..." + uriString);
    }

}
