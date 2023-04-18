package trabalho1.clients.rest;

import jakarta.ws.rs.ProcessingException;
import jakarta.ws.rs.client.Client;
import jakarta.ws.rs.client.ClientBuilder;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.Response.Status;
import org.glassfish.jersey.client.ClientConfig;
import org.glassfish.jersey.client.ClientProperties;
import trabalho1.api.java.Result;
import trabalho1.api.java.Result.ErrorCode;

import java.net.URI;
import java.util.function.Supplier;
import java.util.logging.Logger;

public class RestClient {

    private static Logger Log = Logger.getLogger(RestClient.class.getName());

    protected static final int READ_TIMEOUT = 5000;
    protected static final int CONNECT_TIMEOUT = 5000;

    protected static final int MAX_RETRIES = 10;

    protected static final int RETRY_SLEEP = 3000;

    final URI serverURI;
    final Client client;
    final ClientConfig config;

    RestClient(URI serverURI) {
        this.serverURI = serverURI;
        this.config = new ClientConfig();

        config.property(ClientProperties.READ_TIMEOUT, READ_TIMEOUT);
        config.property(ClientProperties.CONNECT_TIMEOUT, CONNECT_TIMEOUT);

        this.client = ClientBuilder.newClient(config);
    }

    protected <T> Result<T> reTry(Supplier<Result<T>> func) {
        for (int i = 0; i < MAX_RETRIES; i++)
            try {
                return func.get();
            } catch (ProcessingException x) {
                Log.fine("Timeout: " + x.getMessage());
                sleep_ms(RETRY_SLEEP);
            } catch (Exception x) {
                x.printStackTrace();
                return Result.error(ErrorCode.INTERNAL_ERROR);
            }
        return Result.error(ErrorCode.TIMEOUT);
    }

    protected <T> Result<T> toJavaResult(Response r, Class<T> entityType) {
        try {
            Status status = r.getStatusInfo().toEnum();
            if (status.toEnum() == Status.OK && r.hasEntity())
                return Result.ok(r.readEntity(entityType));
            else {
                if (status == Status.NO_CONTENT)
                    return Result.ok();
            }
            return Result.error(getErrorCodeFrom(status.getStatusCode()));
        } finally {
            r.close();
        }
    }

    public static ErrorCode getErrorCodeFrom(int status){
        return switch (status){
            case 200, 209 -> ErrorCode.OK;
            case 400 -> ErrorCode.BAD_REQUEST;
            case 401 -> ErrorCode.UNAUTHORIZED;
            case 403 -> ErrorCode.FORBIDDEN;
            case 404 -> ErrorCode.NOT_FOUND;
            case 405 -> ErrorCode.METHOD_NOT_ALLOWED;
            case 406 -> ErrorCode.NOT_ACCEPTABLE;
            case 409 -> ErrorCode.CONFLICT;
            case 500 -> ErrorCode.INTERNAL_ERROR;
            case 503 -> ErrorCode.SERVICE_UNAVAILABLE;
            case 501 -> ErrorCode.NOT_IMPLEMENTED;
            default -> ErrorCode.INTERNAL_ERROR;
        };
    }

    public String getServerURI() {
        return serverURI.toString();
    }

    private void sleep_ms(int retrySleep) {
        try{
            Thread.sleep(retrySleep);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}
