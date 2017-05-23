package nl.futureedge.simple.jmx.authenticator;

import java.security.Principal;

/**
 * Static authenticator.
 */
public final class StaticAuthenticator extends AbstractAuthenticator {

    /**
     * Create a new default authenticator.
     */
    public StaticAuthenticator(final Principal... principals) {
        super("StaticAuthenticator", new StaticConfiguration("StaticAuthenticator", principals));
    }
}
