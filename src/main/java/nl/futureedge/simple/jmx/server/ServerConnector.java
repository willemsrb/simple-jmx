package nl.futureedge.simple.jmx.server;

import java.io.IOException;
import java.net.MalformedURLException;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.management.MBeanServer;
import javax.management.remote.JMXAuthenticator;
import javax.management.remote.JMXConnectorServer;
import javax.management.remote.JMXServiceURL;
import nl.futureedge.simple.jmx.access.AllAccessController;
import nl.futureedge.simple.jmx.access.DefaultAccessController;
import nl.futureedge.simple.jmx.access.JMXAccessController;
import nl.futureedge.simple.jmx.access.PropertiesAccessController;
import nl.futureedge.simple.jmx.authenticator.ExternalAuthenticator;
import nl.futureedge.simple.jmx.authenticator.PropertiesAuthenticator;
import nl.futureedge.simple.jmx.authenticator.StaticAuthenticator;
import nl.futureedge.simple.jmx.utils.PropertiesFileLoader;

/**
 * Server connector.
 */
final class ServerConnector extends JMXConnectorServer {

    private static final Logger LOGGER = Logger.getLogger(ServerConnector.class.getName());

    private JMXServiceURL url;
    private final Map<String, ?> environment;
    private Thread serverListenerThread;
    private ServerListener serverListener;

    /**
     * Create a new server connector.
     * @param url jmx service url
     * @param environment jmx environment
     * @param server mbean server
     */
    ServerConnector(final JMXServiceURL url, final Map<String, ?> environment, final MBeanServer server) {
        super(server);
        this.url = url;
        this.environment = environment == null ? new HashMap<>() : new HashMap<>(environment);
    }

    @Override
    public JMXServiceURL getAddress() {
        return url;
    }

    void updateAddress(final int localPort) {
        try {
            url = new JMXServiceURL(url.getProtocol(), url.getHost(), localPort);
        } catch (final MalformedURLException e) {
            LOGGER.log(Level.INFO, "Could not update url in JMXConnectorServer to reflect bound port", e);
        }
    }

    @Override
    public Map<String, ?> getAttributes() {
        return Collections.unmodifiableMap(environment);
    }

    @Override
    public boolean isActive() {
        return serverListenerThread != null && serverListenerThread.isAlive();
    }

    @Override
    public synchronized void start() throws IOException {
        if (serverListenerThread != null) {
            return;
        }

        serverListener = new ServerListener(this, determineAuthenticator(environment), determineAccessController(environment));
        serverListenerThread = new Thread(serverListener, "simple-jmx-server-" + serverListener.getServerId());
        serverListenerThread.start();
    }

    private JMXAuthenticator determineAuthenticator(final Map<String, ?> environment) {
        // Custom authenticator via the environment
        final JMXAuthenticator custom = (JMXAuthenticator) environment.get(JMXConnectorServer.AUTHENTICATOR);
        if (custom != null) {
            return custom;
        }

        // External JAAS configuration via System property or environment
        final String sunLoginConfig = System.getProperty("com.sun.management.jmxremote.login.config",
                (String) environment.get("jmx.remote.x.login.config"));
        if (sunLoginConfig != null && !"".equals(sunLoginConfig)) {
            return new ExternalAuthenticator(sunLoginConfig);
        }

        // Property file based authentication via System property or environment
        final String sunPasswordFile = System.getProperty("com.sun.management.jmxremote.password.file",
                (String) environment.get("jmx.remote.x.password.file"));
        if (sunPasswordFile != null && !"".equals(sunPasswordFile)) {
            return new PropertiesAuthenticator(new PropertiesFileLoader(sunPasswordFile));
        }

        // Default: no authentication
        return new StaticAuthenticator();
    }

    private JMXAccessController determineAccessController(final Map<String, ?> environment) {
        // Custom access control via the environment
        final JMXAccessController custom = (JMXAccessController) environment.get("jmx.remote.accesscontroller");
        if (custom != null) {
            return custom;
        }

        // Property file based access control via System property or environment
        final String accessFile = System.getProperty("com.sun.management.jmxremote.access.file",
                (String) environment.get("jmx.remote.x.access.file"));
        if (accessFile != null && !"".equals(accessFile)) {
            return new PropertiesAccessController(new PropertiesFileLoader(accessFile));
        }

        // All access via environment
        if ("all".equals(environment.get("jmx.remote.x.access.type"))) {
            return new AllAccessController();
        }

        // Default: readonly access
        return new DefaultAccessController();
    }


    @Override
    public void stop() throws IOException {
        LOGGER.log(Level.FINE, "Stopping; interrupting listener thread");
        serverListener.stop();
        try {
            LOGGER.log(Level.FINE, "Joining listener thread");
            serverListenerThread.join();
        } catch (final InterruptedException e) {
            LOGGER.log(Level.FINE, "Stop interrupted");
            Thread.currentThread().interrupt();
        }
        LOGGER.log(Level.FINE, "Server stopped");
    }

}
