package nl.futureedge.simple.jmx.server;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import javax.management.remote.JMXServiceURL;
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
        subject.start();
        Assert.assertTrue(subject.isActive());
        // Second start should not do anything
        subject.start();
        Assert.assertTrue(subject.isActive());

        // Stop
        subject.stop();
        Assert.assertFalse(subject.isActive());
        // Second stop should not do anything
        subject.stop();
        Assert.assertFalse(subject.isActive());
    }
}
