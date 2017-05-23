package nl.futureedge.simple.jmx.server;

import javax.management.remote.JMXConnectorServer;

/**
 * Environment constants.
 */
public final class Environment {

    private Environment() {
        throw new IllegalStateException("Do not instantiate");
    }

    public static final String KEY_AUTHENTICATOR = JMXConnectorServer.AUTHENTICATOR;

    public static final String KEY_ACCESSCONTROLLER = "jmx.remote.accesscontroller";

}
