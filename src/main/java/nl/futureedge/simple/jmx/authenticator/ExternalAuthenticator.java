package nl.futureedge.simple.jmx.authenticator;

/**
 * An authenticator based on an external JAAS configuration.
 */
public final class ExternalAuthenticator extends AbstractAuthenticator {

    /**
     * Create a new external authenticator.
     * @param name name of the JAAS login configuration
     */
    public ExternalAuthenticator(final String name) {
        super(name, null);
    }

}
