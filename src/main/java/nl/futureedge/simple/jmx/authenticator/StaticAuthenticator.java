package nl.futureedge.simple.jmx.authenticator;

import java.security.Principal;

/**
 * Static authenticator.
 */
public final class StaticAuthenticator extends AbstractAuthenticator {

    /**
     * Create a new default authenticator.
     * @param principals principals to add to each subject
     */
    public StaticAuthenticator(final Principal... principals) {
        super("StaticAuthenticator", new StaticConfiguration("StaticAuthenticator", principals));
    }
}
