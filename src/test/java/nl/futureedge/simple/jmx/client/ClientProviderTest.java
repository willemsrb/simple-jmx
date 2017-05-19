package nl.futureedge.simple.jmx.client;

import java.net.MalformedURLException;
import javax.management.remote.JMXConnector;
import javax.management.remote.JMXServiceURL;
import nl.futureedge.simple.jmx.SimpleJmx;
import org.junit.Assert;
import org.junit.Test;

public class ClientProviderTest {

    @Test
    public void test() throws MalformedURLException {
        final JMXConnector connector = new ClientProvider().newJMXConnector(new JMXServiceURL(SimpleJmx.PROTOCOL, "localhost", 0), null);
        Assert.assertNotNull(connector);
    }

    @Test(expected = MalformedURLException.class)
    public void testInvalidProtocol() throws MalformedURLException {
        new ClientProvider().newJMXConnector(new JMXServiceURL("invalid", "localhost", 0), null);
    }
}
