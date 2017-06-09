package nl.futureedge.simple.jmx.it;

import java.io.IOException;
import java.io.NotSerializableException;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.LogManager;
import java.util.logging.Logger;
import javax.management.Attribute;
import javax.management.ListenerNotFoundException;
import javax.management.MBeanInfo;
import javax.management.MBeanServerConnection;
import javax.management.Notification;
import javax.management.NotificationListener;
import javax.management.ObjectName;
import javax.management.remote.JMXConnectionNotification;
import javax.management.remote.JMXConnector;
import javax.management.remote.JMXConnectorFactory;
import javax.management.remote.JMXConnectorServer;
import javax.management.remote.JMXServiceURL;
import nl.futureedge.simple.jmx.exception.InvalidCredentialsException;
import nl.futureedge.simple.jmx.stream.MessageException;
import org.awaitility.Awaitility;
import org.junit.Assert;
import org.junit.Test;
import org.slf4j.bridge.SLF4JBridgeHandler;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

public class SimpleJmxIT {

    static {
        // Configure java.util.logging to log via SLF4j
        SLF4JBridgeHandler.removeHandlersForRootLogger();
        SLF4JBridgeHandler.install();
        LogManager.getLogManager().getLogger("").setLevel(Level.FINEST);
    }

    private static final Logger LOGGER = Logger.getLogger(SimpleJmxIT.class.getName());

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
            environment.put(JMXConnector.CREDENTIALS, new String[]{"admin", "admin"});

            LOGGER.log(Level.INFO, "Connect ...");
            try (final JMXConnector jmxc = JMXConnectorFactory.connect(
                    new JMXServiceURL("service:jmx:simple://localhost:" + testServerConnectorPort), environment)) {
                LOGGER.log(Level.INFO, "Connected ...");
                Assert.assertNotNull(jmxc.getConnectionId());
                final MBeanServerConnection serverConnection = jmxc.getMBeanServerConnection();

                for (final ObjectName objectName : serverConnection.queryNames(null, null)) {
                    LOGGER.log(Level.INFO, "Object: {0}", objectName);
                }

                final ObjectName mbeanName = new ObjectName("nl.futureedge.simple.jmx.test:name=TEST");
                final MBeanInfo mBeanInfo = serverConnection.getMBeanInfo(mbeanName);
                LOGGER.log(Level.INFO, "MBean info: {0}", mBeanInfo);
                Assert.assertNotNull(mBeanInfo);

                Assert.assertEquals(Integer.valueOf(42), serverConnection.getAttribute(mbeanName, "ReadonlyAttribuut"));
                Assert.assertEquals(Integer.valueOf(10), serverConnection.getAttribute(mbeanName, "WritableAttribuut"));

                serverConnection.setAttribute(mbeanName, new Attribute("WritableAttribuut", 26));
                Assert.assertEquals(Integer.valueOf(26), serverConnection.getAttribute(mbeanName, "WritableAttribuut"));

                Assert.assertEquals("All ok", serverConnection.invoke(mbeanName, "methodWithReturn", null, null));

                try {
                    serverConnection.getAttribute(mbeanName, "SerializationProblem");
                    Assert.fail("Should fail on a NotSerializableException");
                } catch(MessageException e) {
                    // Expected
                    Assert.assertEquals(NotSerializableException.class, e.getCause().getClass());
                }
                try {
                    serverConnection.getAttribute(mbeanName, "SerializationWriteObjectProblem");
                    Assert.fail("Should fail on a NotSerializableException");
                } catch(MessageException e) {
                    // Expected
                    Assert.assertEquals(NotSerializableException.class, e.getCause().getClass());
                }



            }
            LOGGER.log(Level.INFO, "Shutdown...");
        }

        LOGGER.log(Level.INFO, "Done...");
    }

    @Test
    public void testNotification() throws Exception {
        LOGGER.log(Level.INFO, "Startup...");
        try (final ConfigurableApplicationContext context = new ClassPathXmlApplicationContext(
                new String[]{"classpath:it-context.xml"})) {
            context.registerShutdownHook();

            final JMXConnectorServer testServerConnector = context.getBean("testServerConnector",
                    JMXConnectorServer.class);
            final int testServerConnectorPort = testServerConnector.getAddress().getPort();

            // Setup client
            final Map<String, Object> environment = new HashMap<>();
            environment.put(JMXConnector.CREDENTIALS, new String[]{"admin", "admin"});

            try (final JMXConnector jmxc = JMXConnectorFactory.connect(
                    new JMXServiceURL("service:jmx:simple://localhost:" + testServerConnectorPort), environment);
                 final JMXConnector jmxc2 = JMXConnectorFactory.connect(
                         new JMXServiceURL("service:jmx:simple://localhost:" + testServerConnectorPort),
                         environment)) {

                Assert.assertNotNull(jmxc.getConnectionId());
                Assert.assertNotNull(jmxc2.getConnectionId());
                Assert.assertNotEquals(jmxc.getConnectionId(), jmxc2.getConnectionId());

                final MBeanServerConnection serverConnection = jmxc.getMBeanServerConnection();
                final MBeanServerConnection serverConnection2 = jmxc2.getMBeanServerConnection();

                for (final ObjectName objectName : serverConnection.queryNames(null, null)) {
                    LOGGER.log(Level.INFO, "Object: {0}", objectName);
                }

                final ObjectName mbeanName = new ObjectName("nl.futureedge.simple.jmx.test:name=TEST");
                final MBeanInfo mBeanInfo = serverConnection.getMBeanInfo(mbeanName);
                LOGGER.log(Level.INFO, "MBean info: {0}", mBeanInfo);
                Assert.assertNotNull(mBeanInfo);

                final JmxListener notificationListener = new JmxListener();
                Assert.assertEquals(0, notificationListener.getNotifications().size());
                serverConnection2.addNotificationListener(mbeanName, notificationListener, null, null);

                final JmxListener mbeanListener = context.getBean(JmxListener.class);
                Assert.assertEquals(0, mbeanListener.getNotifications().size());
                serverConnection2.addNotificationListener(mbeanName,
                        new ObjectName("nl.futureedge.simple.jmx.test:name=LISTENER"), null, null);

                serverConnection.setAttribute(mbeanName, new Attribute("WritableAttribuut", 26));
                Assert.assertEquals(Integer.valueOf(26), serverConnection.getAttribute(mbeanName, "WritableAttribuut"));

                Assert.assertEquals("All ok", serverConnection.invoke(mbeanName, "methodWithReturn", null, null));

                TimeUnit.MILLISECONDS.sleep(1000);

                Assert.assertEquals("Local listener did not receive a notification", 1,
                        notificationListener.getNotifications().size());
                Assert.assertEquals("Remote listener did not receive a notification", 1,
                        mbeanListener.getNotifications().size());
            }
            LOGGER.log(Level.INFO, "Shutdown...");
        }

        LOGGER.log(Level.INFO, "Done...");
    }

    @Test
    public void logonFailed() throws IOException {
        LOGGER.log(Level.INFO, "Startup...");
        try (ConfigurableApplicationContext context = new ClassPathXmlApplicationContext(
                new String[]{"classpath:it-context.xml"})) {
            context.registerShutdownHook();

            final JMXConnectorServer testServerConnector = context.getBean("testServerConnector",
                    JMXConnectorServer.class);
            final int testServerConnectorPort = testServerConnector.getAddress().getPort();

            // Logon error
            try (final JMXConnector jmxc = JMXConnectorFactory
                    .connect(new JMXServiceURL("service:jmx:simple://localhost:" + testServerConnectorPort), null)) {
                Assert.fail("Should fail");
            } catch (final IOException e) {
                LOGGER.log(Level.INFO, "Received exception", e);
                // Ok
                Assert.assertEquals(InvalidCredentialsException.class, e.getCause().getClass());
            }

            LOGGER.log(Level.INFO, "Shutdown...");
        }

        LOGGER.log(Level.INFO, "Done...");
    }

    @Test
    public void testClientUnconnected() throws IOException {
        LOGGER.log(Level.INFO, "Startup...");
        try (ConfigurableApplicationContext context = new ClassPathXmlApplicationContext(
                new String[]{"classpath:it-context.xml"})) {
            context.registerShutdownHook();

            final JMXConnectorServer testServerConnector = context.getBean("testServerConnector",
                    JMXConnectorServer.class);
            final int testServerConnectorPort = testServerConnector.getAddress().getPort();

            final Map<String, Object> environment = new HashMap<>();
            environment.put(JMXConnector.CREDENTIALS, new String[]{"admin", "admin"});
            try (final JMXConnector jmxc = JMXConnectorFactory.newJMXConnector(
                    new JMXServiceURL("service:jmx:simple://localhost:" + testServerConnectorPort), environment)) {
                try {
                    jmxc.getConnectionId();
                    Assert.fail("IOException (not connected) expected");
                } catch (final IOException e) {
                    // Expected
                }

                try {
                    jmxc.getMBeanServerConnection();
                    Assert.fail("IOException (not connected) expected");
                } catch (final IOException e) {
                    // Expected
                }

                jmxc.connect();
                Assert.assertNotNull(jmxc.getConnectionId());
            }

            LOGGER.log(Level.INFO, "Shutdown...");
        }

        LOGGER.log(Level.INFO, "Done...");
    }

    @Test
    public void testConnectionNotifier() throws IOException, ListenerNotFoundException, InterruptedException {
        LOGGER.log(Level.INFO, "Startup...");
        try (ConfigurableApplicationContext context = new ClassPathXmlApplicationContext(
                new String[]{"classpath:it-context.xml"})) {
            context.registerShutdownHook();

            final JMXConnectorServer testServerConnector = context.getBean("testServerConnector",
                    JMXConnectorServer.class);
            final int testServerConnectorPort = testServerConnector.getAddress().getPort();

            final Map<String, Object> environment = new HashMap<>();
            environment.put(JMXConnector.CREDENTIALS, new String[]{"admin", "admin"});

            final Counter counter = new Counter();
            try (final JMXConnector jmxc1 = JMXConnectorFactory.newJMXConnector(
                    new JMXServiceURL("service:jmx:simple://localhost:" + testServerConnectorPort), environment);
                 final JMXConnector jmxc2 = JMXConnectorFactory.newJMXConnector(
                         new JMXServiceURL("service:jmx:simple://localhost:" + testServerConnectorPort),
                         environment)) {
                jmxc1.addConnectionNotificationListener(counter, null, null);
                jmxc2.addConnectionNotificationListener(counter, null, null);

                Assert.assertEquals(0, counter.opened);
                Assert.assertEquals(0, counter.closed);

                jmxc1.connect();

                // Wait for threaded notification
                Awaitility.await().atMost(1, TimeUnit.SECONDS).until(() -> counter.opened == 1);
                Assert.assertEquals(0, counter.closed);

                jmxc2.connect();

                // Wait for threaded notification
                Awaitility.await().atMost(1, TimeUnit.SECONDS).until(() -> counter.opened == 2);
                Assert.assertEquals(0, counter.closed);

                jmxc2.removeConnectionNotificationListener(counter);
            }

            // Wait for threaded notification
            Awaitility.await().atMost(1, TimeUnit.SECONDS).until(() -> counter.closed == 1);
            Assert.assertEquals(2, counter.opened);

            LOGGER.log(Level.INFO, "Shutdown...");
        }

        LOGGER.log(Level.INFO, "Done...");
    }

    private static final class Counter implements NotificationListener {

        int opened = 0;
        int closed = 0;

        @Override
        public void handleNotification(final Notification notification, final Object handback) {
            if (JMXConnectionNotification.OPENED.equals(notification.getType())) {
                opened++;
            } else if (JMXConnectionNotification.CLOSED.equals(notification.getType())) {
                closed++;
            } else {
                throw new IllegalArgumentException("Unknown notification");
            }
        }
    }

}
