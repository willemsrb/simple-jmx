package nl.futureedge.simple.jmx.message;

/**
 * Logon request.
 */
public final class RequestLogon extends Request {

    private static final long serialVersionUID = 1L;

    private final String username;
    private final String password;

    /**
     * Constructor.
     * @param username username
     * @param password password
     */
    public RequestLogon(final String username, final String password) {
        super();
        this.username = username;
        this.password = password;
    }

    public String getUsername() {
        return username;
    }

    public String getPassword() {
        return password;
    }

    @Override
    public String toString() {
        return "RequestLogon [requestId=" + getRequestId() + ", username=" + username + "]";
    }

}
