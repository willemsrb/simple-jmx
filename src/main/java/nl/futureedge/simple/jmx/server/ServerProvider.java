package nl.futureedge.simple.jmx.server;

import java.net.MalformedURLException;
import java.util.Map;

import javax.management.MBeanServer;
import javax.management.remote.JMXConnectorServer;
import javax.management.remote.JMXConnectorServerProvider;
import javax.management.remote.JMXServiceURL;

import nl.futureedge.simple.jmx.SimpleJmx;

/**
 * JMX Server provider.
 */
public final class ServerProvider implements JMXConnectorServerProvider {

	@Override
	public JMXConnectorServer newJMXConnectorServer(final JMXServiceURL url, final Map<String, ?> environment,
			final MBeanServer server) throws MalformedURLException {
		final String protocol = url.getProtocol();
		if (!SimpleJmx.PROTOCOL.equals(protocol)) {
			throw new MalformedURLException(
					"Invalid protocol '" + protocol + "' for provider " + this.getClass().getName());
		}
		return new ServerConnector(url, environment, server);
	}
}
