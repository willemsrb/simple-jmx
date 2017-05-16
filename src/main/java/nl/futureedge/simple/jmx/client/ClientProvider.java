package nl.futureedge.simple.jmx.client;

import java.net.MalformedURLException;
import java.util.Map;
import javax.management.remote.JMXConnector;
import javax.management.remote.JMXConnectorProvider;
import javax.management.remote.JMXServiceURL;
import nl.futureedge.simple.jmx.SimpleJmx;

/**
 * JMX Client provider.
 */
public final class ClientProvider implements JMXConnectorProvider {

    @Override
    public JMXConnector newJMXConnector(final JMXServiceURL url, final Map<String, ?> environment)
            throws MalformedURLException {
        final String protocol = url.getProtocol();
        if (!SimpleJmx.PROTOCOL.equals(protocol)) {
            throw new MalformedURLException(
                    "Invalid protocol '" + protocol + "' for provider " + this.getClass().getName());
        }

        return new ClientConnector(url, environment);
    }
}
