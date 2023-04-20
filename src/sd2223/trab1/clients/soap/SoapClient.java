package sd2223.trab1.clients.soap;

import com.sun.xml.ws.client.BindingProviderProperties;
import jakarta.xml.ws.BindingProvider;
import jakarta.xml.ws.WebServiceException;
import sd2223.trab1.api.java.Result;
import sd2223.trab1.api.java.Result.ErrorCode;

import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.util.logging.Logger;

import static sd2223.trab1.api.java.Result.ok;

abstract class SoapClient {
    protected static final int READ_TIMEOUT = 5000;
    protected static final int CONNECT_TIMEOUT = 5000;

    protected static final int MAX_RETRIES = 10;
    protected static final int RETRY_SLEEP = 3000;

    private static Logger Log = Logger.getLogger(SoapClient.class.getName());

    protected static final String WSDL = "?wsdl";

    protected final URI uri;

    public SoapClient(URI uri) {
        this.uri = uri;
    }

    protected void setTimeouts(BindingProvider port) {
        port.getRequestContext().put(BindingProviderProperties.CONNECT_TIMEOUT, CONNECT_TIMEOUT);
        port.getRequestContext().put(BindingProviderProperties.REQUEST_TIMEOUT, READ_TIMEOUT);
    }

    protected <T> Result<T> reTry(ResultSupplier<Result<T>> func) {
        for (int i = 0; i < MAX_RETRIES; i++)
            try {
                return func.get();
            } catch (WebServiceException x) {
                x.printStackTrace();
                // Log.fine("Timeout: " + x.getMessage());
                sleep_ms(RETRY_SLEEP);
            } catch (Exception x) {
                x.printStackTrace();
                return Result.error(ErrorCode.INTERNAL_ERROR);
            }
        return Result.error(ErrorCode.TIMEOUT);
    }

    protected <R> Result<R> toJavaResult(ResultSupplier<R> supplier) {
        try {
            return ok(supplier.get());
        } catch (Exception e) {
            e.printStackTrace();
            return Result.error(getErrorCode(e));
        }
    }

    protected <R> Result<R> toJavaResult(VoidSupplier r) {
        try {
            r.run();
            return ok();
        } catch (WebServiceException x) {
            throw x;
        } catch (Exception e) {
            e.printStackTrace();
            return Result.error(getErrorCode(e));
        }
    }

    private ErrorCode getErrorCode(Exception e) {
        try {
            return ErrorCode.valueOf(e.getMessage());
        } catch (IllegalArgumentException ex) {
            return ErrorCode.INTERNAL_ERROR;
        }
    }

    static interface ResultSupplier<T> {
        T get() throws Exception;
    }

    static interface VoidSupplier {
        void run() throws Exception;
    }

    public String getServerURI() {
        return uri.toString();
    }

    public static URL toURL(String url) {
        try {
            return new URL(url);
        } catch (MalformedURLException e) {
            e.printStackTrace();
        }
        return null;
    }

    protected void sleep_ms(int ms) {
        try {
            Thread.sleep(ms);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

}
