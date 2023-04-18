package trabalho1.clients;

import trabalho1.api.java.Feeds;
import trabalho1.api.java.Users;
import trabalho1.clients.rest.RestFeedsClient;
import trabalho1.clients.rest.RestUsersClient;
import trabalho1.clients.soap.SoapFeedsClient;
import trabalho1.clients.soap.SoapUsersClient;

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
