package nl.futureedge.simple.jmx.it;

import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.LogManager;
import java.util.logging.Logger;
import javax.management.Attribute;
import javax.management.MBeanServerConnection;
import javax.management.ObjectName;
import javax.management.remote.JMXConnector;
import javax.management.remote.JMXConnectorFactory;
import javax.management.remote.JMXConnectorServer;
import javax.management.remote.JMXServiceURL;
import org.junit.Assert;
import org.junit.Test;
import org.slf4j.bridge.SLF4JBridgeHandler;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

public class SecurityIT {

    static {
        // Configure java.util.logging to log via SLF4j
        SLF4JBridgeHandler.removeHandlersForRootLogger();
        SLF4JBridgeHandler.install();
        LogManager.getLogManager().getLogger("").setLevel(Level.FINEST);
    }

    private static final Logger LOGGER = Logger.getLogger(SecurityIT.class.getName());

    @Test
    public void testBase() throws Exception {
        LOGGER.log(Level.INFO, "Startup ...");
        try (final ConfigurableApplicationContext context = new ClassPathXmlApplicationContext(
                new String[]{"classpath:it-context.xml"})) {
            context.registerShutdownHook();

            final JMXConnectorServer testServerConnector = context.getBean("testServerConnector",
                    JMXConnectorServer.class);
            final int testServerConnectorPort = testServerConnector.getAddress().getPort();

            // Setup client
            final Map<String, Object> environment = new HashMap<>();
            environment.put(JMXConnector.CREDENTIALS, new String[]{"reader", "reader"});

            LOGGER.log(Level.INFO, "Connect ...");
            try (final JMXConnector jmxc = JMXConnectorFactory.connect(
                    new JMXServiceURL("service:jmx:simple://localhost:" + testServerConnectorPort), environment)) {
                LOGGER.log(Level.INFO, "Connected ...");
                final MBeanServerConnection serverConnection = jmxc.getMBeanServerConnection();

                final ObjectName mbeanName = new ObjectName("nl.futureedge.simple.jmx.test:name=TEST");

                // Readable attribute
                Assert.assertEquals(Integer.valueOf(42), serverConnection.getAttribute(mbeanName, "ReadonlyAttribuut"));

                // Writable attribute
                Assert.assertEquals(Integer.valueOf(10), serverConnection.getAttribute(mbeanName, "WritableAttribuut"));
                try {
                    serverConnection.setAttribute(mbeanName, new Attribute("WritableAttribuut", 26));
                    Assert.fail("Should not be allowed to write an attribute");
                } catch (final SecurityException e) {
                    // Expected
                }
                Assert.assertEquals(Integer.valueOf(10), serverConnection.getAttribute(mbeanName, "WritableAttribuut"));

                // Method
                try {
                    serverConnection.invoke(mbeanName, "methodWithReturn", null, null);
                    Assert.fail("Should not be allowed to invoke a method");
                } catch (final SecurityException e) {
                    // Expected
                }
            }
            LOGGER.log(Level.INFO, "Shutdown...");
        }

        LOGGER.log(Level.INFO, "Done...");
    }

}
