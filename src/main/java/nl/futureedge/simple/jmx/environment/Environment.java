package nl.futureedge.simple.jmx.environment;

import java.io.IOException;
import java.util.Map;
import javax.management.remote.JMXAuthenticator;
import javax.management.remote.JMXConnector;
import javax.management.remote.JMXConnectorServer;
import nl.futureedge.simple.jmx.access.DefaultAccessController;
import nl.futureedge.simple.jmx.access.JMXAccessController;
import nl.futureedge.simple.jmx.authenticator.StaticAuthenticator;
import nl.futureedge.simple.jmx.socket.AnonymousSslSocketFactory;
import nl.futureedge.simple.jmx.socket.JMXSocketFactory;
import nl.futureedge.simple.jmx.socket.SslConfigurationException;

/**
 * Environment constants.
 */
public final class Environment {

    private Environment() {
        throw new IllegalStateException("Do not instantiate");
    }

    public static final String KEY_SOCKETFACTORY = "jmx.remote.socketfactory";

    public static final String KEY_AUTHENTICATOR = JMXConnectorServer.AUTHENTICATOR;

    public static final String KEY_ACCESSCONTROLLER = "jmx.remote.accesscontroller";

    public static final String KEY_REQUESTTIMEOUT = "jmx.remote.requesttimeout";

    public static final String KEY_CREDENTIALS = JMXConnector.CREDENTIALS;


    public static JMXSocketFactory determineSocketFactory(final Map<String, ?> environment) throws IOException {
        // Custom socket factory via the environment
        final JMXSocketFactory custom = (JMXSocketFactory) environment.get(KEY_SOCKETFACTORY);
        if (custom != null) {
            return custom;
        }

        // Default: no authentication
        try {
            return new AnonymousSslSocketFactory();
        } catch (final SslConfigurationException e) {
            throw new IOException("Could not create default socket factory", e);
        }
    }

    public static JMXAuthenticator determineAuthenticator(final Map<String, ?> environment) {
        // Custom authenticator via the environment
        final JMXAuthenticator custom = (JMXAuthenticator) environment.get(KEY_AUTHENTICATOR);
        if (custom != null) {
            return custom;
        }

        // Default: no authentication
        return new StaticAuthenticator();
    }

    public static JMXAccessController determineAccessController(final Map<String, ?> environment) {
        // Custom access control via the environment
        final JMXAccessController custom = (JMXAccessController) environment.get(KEY_ACCESSCONTROLLER);
        if (custom != null) {
            return custom;
        }

        // Default: readonly access
        return new DefaultAccessController();
    }


    public static Object determineCredentials(final Map<String, ?> environment) {
        return environment.get(KEY_CREDENTIALS);
    }

    public static int determineRequestTimeout(final Map<String, ?> environment) {
        final Object timeout = environment.get(KEY_REQUESTTIMEOUT);
        if (timeout instanceof Number) {
            // Custom timeout
            return ((Number) timeout).intValue();
        } else if (timeout instanceof String) {
            // Custom timeout
            return Integer.parseInt((String) timeout);
        } else if (timeout == null) {
            // Default: 15 seconds
            return 15;
        } else {
            // Invalid object
            throw new IllegalArgumentException("Environment key " + KEY_REQUESTTIMEOUT + " should contain a Number (or a String)");
        }
    }
}
