package nl.futureedge.simple.jmx.server;

import java.io.IOException;
import java.net.MalformedURLException;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.management.MBeanServer;
import javax.management.remote.JMXConnectorServer;
import javax.management.remote.JMXServiceURL;

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
	 * Constructor.
	 * 
	 * @param url
	 *            jmx service url
	 * @param environment
	 *            jmx environment
	 * @param server
	 *            mbean server
	 */
	ServerConnector(JMXServiceURL url, Map<String, ?> environment, MBeanServer server) {
		super(server);
		this.url = url;
		this.environment = new HashMap<>(environment);
	}

	@Override
	public JMXServiceURL getAddress() {
		return url;
	}

	void updateAddress(int localPort) {
		try {
			url = new JMXServiceURL(url.getProtocol(), url.getHost(), localPort);
		} catch (MalformedURLException e) {
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

		serverListener = new ServerListener(this);
		serverListenerThread = new Thread(serverListener, "simple-jmx-server-" + serverListener.getServerId());
		serverListenerThread.start();
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
