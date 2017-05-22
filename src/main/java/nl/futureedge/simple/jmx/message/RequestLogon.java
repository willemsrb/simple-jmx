package nl.futureedge.simple.jmx.message;

/**
 * Logon request.
 */
public final class RequestLogon extends Request {

    private static final long serialVersionUID = 1L;

    private final Object credentials;

    /**
     * Create a new logon request.
     * @param credentials credentials
     */
    public RequestLogon(final Object credentials) {
        this.credentials = credentials;
    }

    public Object getCredentials() {
        return credentials;
    }

    @Override
    public String toString() {
        return "RequestLogon [requestId=" + getRequestId() + ", credentials=" + (credentials == null ? "***not supplied***" : "***supplied***") + "]";
    }

}
