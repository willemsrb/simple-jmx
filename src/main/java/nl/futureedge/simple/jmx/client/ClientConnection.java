package nl.futureedge.simple.jmx.client;

import java.io.IOException;
import java.net.Socket;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.management.NotificationFilter;
import javax.management.NotificationListener;
import javax.management.ObjectName;
import javax.management.remote.JMXConnector;
import javax.management.remote.JMXServiceURL;

import nl.futureedge.simple.jmx.message.Request;
import nl.futureedge.simple.jmx.message.RequestLogoff;
import nl.futureedge.simple.jmx.message.RequestLogon;
import nl.futureedge.simple.jmx.message.Response;
import nl.futureedge.simple.jmx.ssl.SslSocketFactory;
import nl.futureedge.simple.jmx.stream.MessageInputStream;
import nl.futureedge.simple.jmx.stream.MessageOutputStream;
import nl.futureedge.simple.jmx.utils.IOUtils;

/**
 * Client connection.
 */
public final class ClientConnection {

	private static final Logger LOGGER = Logger.getLogger(ClientConnection.class.getName());

	private final JMXServiceURL serviceUrl;
	private final Map<String, ?> environment;

	private Socket socket;
	private MessageInputStream input;
	private MessageOutputStream output;

	private ClientListener clientListener;
	private Thread clientListenerThread;

	private String connectionId;

	/**
	 * Constructor.
	 * 
	 * @param serviceUrl
	 *            jmx service url
	 * @param environment
	 *            environment
	 */
	public ClientConnection(final JMXServiceURL serviceUrl, final Map<String, ?> environment) {
		this.serviceUrl = serviceUrl;
		this.environment = environment;
	}

	/**
	 * Get the connection id returned when the logon command is executed.
	 * 
	 * @return connection id
	 * @throws IOException
	 *             if the connection is not connected
	 */
	public String getConnectionId() throws IOException {
		if (connectionId == null) {
			throw new IOException("Not connected");
		}
		return connectionId;
	}

	/**
	 * Connect the connection to the server.
	 * 
	 * @throws IOException
	 *             if an I/O error occurs when connecting
	 */
	public void connect() throws IOException {
		LOGGER.log(Level.FINE, "Connecting to {0}:{1,number,#####} ...",
				new Object[] { serviceUrl.getHost(), serviceUrl.getPort() });
		socket = new SslSocketFactory().createSocket(serviceUrl);

		// The socket InputStream and OutputStream are not closed directly. They
		// are shutdown and closed via method calls on the socket itself.
		output = new MessageOutputStream(socket.getOutputStream());
		input = new MessageInputStream(socket.getInputStream());

		LOGGER.log(Level.FINE, "Starting receiver");
		clientListener = new ClientListener(input);
		clientListenerThread = new Thread(clientListener, "jmx-client-receiver");
		clientListenerThread.start();

		LOGGER.log(Level.FINE, "Sending logon request");
		final String[] credentials = (String[]) environment.get(JMXConnector.CREDENTIALS);
		final String username = credentials == null || credentials.length < 1 ? null : credentials[0];
		final String password = credentials == null || credentials.length < 2 ? null : credentials[1];
		final RequestLogon logon = new RequestLogon(username, password);

		LOGGER.log(Level.FINE, "Handling logon response");
		final Response logonResponse = handleRequest(logon);
		if (logonResponse != null && logonResponse.getException() != null) {
			LOGGER.log(Level.FINE, "Logon failed");
			throw new IOException("Could not logon", logonResponse.getException());
		}
		connectionId = (String) logonResponse.getResult();

		LOGGER.log(Level.FINE, "Connected; connectionId = {0}", connectionId);
	}

	/**
	 * Close the connection.
	 * 
	 * @throws IOException
	 *             if an I/O error occurs when closing the connection
	 */
	public void close() throws IOException {
		LOGGER.log(Level.FINE, "Sending logoff");
		try {
			handleRequest(new RequestLogoff());
		} catch (final IOException e) {
			LOGGER.log(Level.WARNING, "Unexpected exception when logging off", e);
		}

		LOGGER.log(Level.FINE, "Stopping client listener");
		clientListener.stop();

		IOUtils.closeSilently(socket);
		LOGGER.log(Level.FINE, "Closed");
	}

	/**
	 * Handle request.
	 * 
	 * @param request
	 *            request
	 * @return response
	 * @throws IOException
	 *             if an I/O error occurs when handling the request
	 */
	public Response handleRequest(final Request request) throws IOException {
		if (!clientListenerThread.isAlive()) {
			throw new IOException("Listener not running");
		}
		send(request);
		return clientListener.getResponseFor(request);
	}

	private void send(final Request request) throws IOException {
		LOGGER.log(Level.FINE, "Sending request: {0}", request);
		synchronized (output) {
			output.write(request);
		}
		LOGGER.log(Level.FINE, "Request sent", request);
	}

	/**
	 * Register a notification listener.
	 * 
	 * @param objectName
	 *            object name to register the listener on
	 * @param notificationListener
	 *            the listener to request
	 * @param notificationFilter
	 *            filter
	 * @param handback
	 *            handback given back to the listener
	 * @return unique identification for notification listener
	 */
	public String registerNotificationListener(final ObjectName objectName,
			final NotificationListener notificationListener, final NotificationFilter notificationFilter,
			final Object handback) {
		return clientListener.registerNotificationListener(objectName, notificationListener, notificationFilter,
				handback);
	}

	/**
	 * Remove a notification listener.
	 * 
	 * @param notificationListenerId
	 *            the unique identification of the notifcation listener
	 */
	public void removeNotificationListener(final String notificationListenerId) {
		clientListener.removeNotificationListener(notificationListenerId);
	}

}
