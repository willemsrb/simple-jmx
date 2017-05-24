package nl.futureedge.simple.jmx.agent;

import java.io.IOException;
import java.lang.management.ManagementFactory;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.management.MBeanServer;
import javax.management.remote.JMXConnectorServer;
import javax.management.remote.JMXServiceURL;
import nl.futureedge.simple.jmx.SimpleJmx;
import nl.futureedge.simple.jmx.access.PropertiesAccessController;
import nl.futureedge.simple.jmx.authenticator.ExternalAuthenticator;
import nl.futureedge.simple.jmx.authenticator.PropertiesAuthenticator;
import nl.futureedge.simple.jmx.environment.Environment;
import nl.futureedge.simple.jmx.server.ServerProvider;
import nl.futureedge.simple.jmx.utils.PropertiesFileLoader;

/**
 * Agent class to startup the JMXConnectionServer
 */
public class Agent {

    private static final Logger LOGGER = Logger.getLogger(Agent.class.getName());

    public static final String SYSTEM_PROPERY_HOST = "nl.futureedge.simple.jmx.host";
    public static final String SYSTEM_PROPERTY_PORT = "nl.futureedge.simple.jmx.port";
    public static final String SYSTEM_PROPERTY_LOGIN_CONFIG = "nl.futureedge.simple.jmx.login.config";
    public static final String SYSTEM_PROPERTY_PASSWORD_FILE = "nl.futureedge.simple.jmx.password.file";
    public static final String SYSTEM_PROPERTY_ACCESS_FILE = "nl.futureedge.simple.jmx.access.file";

    /**
     * Starting point when used as a java agent loaded at startup
     * @param agentArgs arguments
     * @throws IOException when an I/O error occurs starting the jmx connector server.
     */
    public static void premain(final String agentArgs) throws IOException {
        agentmain(agentArgs);
    }

    /**
     * Starting point when used as a java agent loaded after startup
     * @param agentArgs argument
     * @throws IOException when an I/O error occurs starting the jmx connector server.
     */
    public static void agentmain(final String agentArgs) throws IOException {
        LOGGER.log(Level.FINE, "Configuring Simple-JMX server connector");

        // Connection
        final String host = System.getProperty(SYSTEM_PROPERY_HOST, "0.0.0.0");
        final int port = Integer.parseInt(System.getProperty(SYSTEM_PROPERTY_PORT, "3481"));
        JMXServiceURL url = new JMXServiceURL(SimpleJmx.PROTOCOL, host, port);

        // Environment
        final Map<String, Object> environment = new HashMap<>();

        // Environment - authentication
        final String loginConfig = System.getProperty(SYSTEM_PROPERTY_LOGIN_CONFIG);
        if (loginConfig != null && !"".equals(loginConfig)) {
            environment.put(Environment.KEY_AUTHENTICATOR, new ExternalAuthenticator(loginConfig));
        } else {
            final String passwordFile = System.getProperty(SYSTEM_PROPERTY_PASSWORD_FILE);
            if (passwordFile != null && !"".equals(passwordFile)) {
                environment.put(Environment.KEY_AUTHENTICATOR, new PropertiesAuthenticator(new PropertiesFileLoader(passwordFile)));
            }
        }

        // Environment - access control
        final String accessFile = System.getProperty(SYSTEM_PROPERTY_ACCESS_FILE);
        if (accessFile != null && !"".equals(accessFile)) {
            environment.put(Environment.KEY_ACCESSCONTROLLER, new PropertiesAccessController(new PropertiesFileLoader(accessFile)));
        }

        // MBean server
        final MBeanServer mbeanServer = ManagementFactory.getPlatformMBeanServer();

        // Server
        LOGGER.log(Level.FINE, "Starting Simple-JMX server connector");
        final JMXConnectorServer server = new ServerProvider().newJMXConnectorServer(url, environment, mbeanServer);
        server.start();

        LOGGER.log(Level.FINE, "Registering Simple-JMX server shutdown hook");
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            try {
                LOGGER.log(Level.FINE, "Simple-JMX server connector stopping");
                server.stop();
                LOGGER.log(Level.FINE, "Simple-JMX server connector stopped");
            } catch (IOException e) {
                LOGGER.log(Level.FINE, "Exception occurred during shutdown of Simple-JMX server.", e);
            }
        }, "simple-jmx-shutdown"));
        LOGGER.log(Level.FINE, "Simple-JMX server connector started");
    }
}
