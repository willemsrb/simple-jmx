package nl.futureedge.simple.jmx.server;

import java.net.MalformedURLException;
import javax.management.remote.JMXConnectorServer;
import javax.management.remote.JMXServiceURL;
import nl.futureedge.simple.jmx.SimpleJmx;
import org.junit.Assert;
import org.junit.Test;

public class ServerProviderTest {

    @Test
    public void test() throws MalformedURLException {
        final JMXConnectorServer connector = new ServerProvider().newJMXConnectorServer(new JMXServiceURL(SimpleJmx.PROTOCOL, "localhost", 0), null, null);
        Assert.assertNotNull(connector);
    }

    @Test(expected = MalformedURLException.class)
    public void testInvalidProtocol() throws MalformedURLException {
        new ServerProvider().newJMXConnectorServer(new JMXServiceURL("invalid", "localhost", 0), null, null);
    }
}
