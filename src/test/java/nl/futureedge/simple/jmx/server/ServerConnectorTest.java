package nl.futureedge.simple.jmx.server;

import java.io.IOException;
import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import javax.management.remote.JMXAuthenticator;
import javax.management.remote.JMXServiceURL;
import nl.futureedge.simple.jmx.access.AllAccessController;
import nl.futureedge.simple.jmx.access.DefaultAccessController;
import nl.futureedge.simple.jmx.access.JMXAccessController;
import nl.futureedge.simple.jmx.authenticator.PropertiesAuthenticator;
import nl.futureedge.simple.jmx.authenticator.StaticAuthenticator;
import nl.futureedge.simple.jmx.environment.Environment;
import nl.futureedge.simple.jmx.utils.IOUtils;
import org.junit.Assert;
import org.junit.Test;

public class ServerConnectorTest {

    @Test
    public void testAddress() throws IOException {
        final JMXServiceURL url = new JMXServiceURL("simple", "localhost", 0);
        final ServerConnector subject = new ServerConnector(url, null, null);

        // Address
        Assert.assertNotNull(subject.getAddress());
        Assert.assertEquals(url, subject.getAddress());

        // Update
        subject.updateAddress(1045);
        Assert.assertNotEquals(url, subject.getAddress());
        Assert.assertEquals(1045, subject.getAddress().getPort());

        // Invalid
        subject.updateAddress(-4534);
        Assert.assertEquals(1045, subject.getAddress().getPort());
    }

    @Test
    public void testAttributes() throws IOException {
        final JMXServiceURL url = new JMXServiceURL("simple", "localhost", 0);
        final Map<String, Object> environment = new HashMap<>();
        environment.put("key", "value");
        final ServerConnector subject = new ServerConnector(url, environment, null);
        Assert.assertEquals(1, subject.getAttributes().size());
        Assert.assertEquals("value", subject.getAttributes().get("key"));
        try {
            subject.getAttributes().remove("bla");
            Assert.fail("Returned map should not be modifiable");
        } catch (final UnsupportedOperationException e) {
            // Expected
        }
    }

    @Test
    public void testStartStop()
            throws IOException {
        final JMXServiceURL url = new JMXServiceURL("simple", "localhost", 0);
        final ServerConnector subject = new ServerConnector(url, null, null);
        Assert.assertFalse(subject.isActive());

        // Start
        try {
            subject.start();
            Assert.assertTrue(subject.isActive());
            // Second start should not do anything
            subject.start();
            Assert.assertTrue(subject.isActive());
        } finally {
            // Stop
            subject.stop();
            Assert.assertFalse(subject.isActive());
            // Second stop should not do anything
            subject.stop();
            Assert.assertFalse(subject.isActive());
        }
    }

    @Test
    public void testDefaultConfig() throws IOException, ReflectiveOperationException {
        final JMXServiceURL url = new JMXServiceURL("simple", "localhost", 0);
        final ServerConnector subject = new ServerConnector(url, null, null);
        try {
            subject.start();

            final Field listenerField = ServerConnector.class.getDeclaredField("serverListener");
            listenerField.setAccessible(true);
            final ServerListener listener = (ServerListener) listenerField.get(subject);

            final Field authenticatorField = ServerListener.class.getDeclaredField("authenticator");
            authenticatorField.setAccessible(true);
            final JMXAuthenticator authenticator = (JMXAuthenticator) authenticatorField.get(listener);
            Assert.assertEquals(StaticAuthenticator.class, authenticator.getClass());

            final Field accessControllerField = ServerListener.class.getDeclaredField("accessController");
            accessControllerField.setAccessible(true);
            final JMXAccessController accessController = (JMXAccessController) accessControllerField.get(listener);
            Assert.assertEquals(DefaultAccessController.class, accessController.getClass());
        } finally {
            IOUtils.ignoreIOException(subject::stop);
        }
    }


    @Test
    public void testCustomConfig() throws IOException, ReflectiveOperationException {
        final JMXServiceURL url = new JMXServiceURL("simple", "localhost", 0);
        final Map<String, Object> environment = new HashMap<>();
        environment.put(Environment.KEY_AUTHENTICATOR, new PropertiesAuthenticator(new Properties()));
        environment.put(Environment.KEY_ACCESSCONTROLLER, new AllAccessController());
        final ServerConnector subject = new ServerConnector(url, environment, null);

        subject.start();

        final Field listenerField = ServerConnector.class.getDeclaredField("serverListener");
        listenerField.setAccessible(true);
        final ServerListener listener = (ServerListener) listenerField.get(subject);

        final Field authenticatorField = ServerListener.class.getDeclaredField("authenticator");
        authenticatorField.setAccessible(true);
        final JMXAuthenticator authenticator = (JMXAuthenticator) authenticatorField.get(listener);
        Assert.assertSame(PropertiesAuthenticator.class, authenticator.getClass());

        final Field accessControllerField = ServerListener.class.getDeclaredField("accessController");
        accessControllerField.setAccessible(true);
        final JMXAccessController accessController = (JMXAccessController) accessControllerField.get(listener);
        Assert.assertEquals(AllAccessController.class, accessController.getClass());

        subject.stop();
    }
}
