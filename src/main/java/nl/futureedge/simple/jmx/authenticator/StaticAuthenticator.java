package nl.futureedge.simple.jmx.authenticator;

/**
 * Static authenticator.
 */
public final class StaticAuthenticator extends AbstractAuthenticator {

    /**
     * Create a new default authenticator.
     */
    public StaticAuthenticator() {
        super("StaticAuthenticator", new StaticConfiguration("StaticAuthenticator"));
    }
}
