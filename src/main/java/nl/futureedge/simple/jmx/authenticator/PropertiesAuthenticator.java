package nl.futureedge.simple.jmx.authenticator;

import java.util.Properties;
import nl.futureedge.simple.jmx.utils.PropertiesLoader;

/**
 * Properties authenticator.
 */
public final class PropertiesAuthenticator extends AbstractAuthenticator {

    /**
     * Create a new properties authenticator.
     * @param properties properties to use
     */
    public PropertiesAuthenticator(final Properties properties) {
        this(() -> properties);
    }

    /**
     * Create a new default authenticator.
     * @param propertiesLoader properties loader to use
     */
    public PropertiesAuthenticator(final PropertiesLoader propertiesLoader) {
        super("PropertiesAuthenticator", new PropertiesConfiguration("PropertiesAuthenticator", propertiesLoader));
    }
}
