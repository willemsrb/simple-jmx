package nl.futureedge.simple.jmx.client;

import java.io.IOException;
import java.net.Socket;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.management.NotificationListener;
import javax.management.remote.JMXConnectionNotification;
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
final class ClientConnection {

    private static final Logger LOGGER = Logger.getLogger(ClientConnection.class.getName());

    private final ClientConnector connector;
    private final JMXServiceURL serviceUrl;
    private final Map<String, ?> environment;

    private Socket socket;
    private MessageOutputStream output;

    private ClientListener clientListener;
    private Thread clientListenerThread;

    private String connectionId;

    /**
     * Create a new client connection.
     * @param connector client connector (to send notifications)
     * @param serviceUrl jmx service url
     * @param environment environment
     */
    ClientConnection(final ClientConnector connector, final JMXServiceURL serviceUrl, final Map<String, ?> environment) {
        this.connector = connector;
        this.serviceUrl = serviceUrl;
        this.environment = environment;
    }

    /**
     * Get the connection id returned when the logon command is executed.
     * @return connection id
     * @throws IOException if the connection is not connected
     */
    String getConnectionId() throws IOException {
        if (connectionId == null) {
            throw new IOException("Not connected");
        }
        return connectionId;
    }

    /**
     * Connect the connection to the server.
     * @throws IOException if an I/O error occurs when connecting
     */
    void connect() throws IOException {
        LOGGER.log(Level.FINE, "Connecting to {0}:{1,number,#####} ...",
                new Object[]{serviceUrl.getHost(), serviceUrl.getPort()});
        socket = new SslSocketFactory().createSocket(serviceUrl);

        // The socket InputStream and OutputStream are not closed directly. They
        // are shutdown and closed via method calls on the socket itself.
        output = new MessageOutputStream(socket.getOutputStream());

        LOGGER.log(Level.FINE, "Starting receiver");
        clientListener = new ClientListener(new MessageInputStream(socket.getInputStream()));
        clientListenerThread = new Thread(clientListener, "jmx-client-receiver");
        clientListenerThread.start();

        LOGGER.log(Level.FINE, "Sending logon request");
        final RequestLogon logon = new RequestLogon(environment.get(JMXConnector.CREDENTIALS));

        LOGGER.log(Level.FINE, "Handling logon response");
        final Response logonResponse = handleRequest(logon);
        if (logonResponse.getException() != null) {
            LOGGER.log(Level.FINE, "Logon failed");
            throw new IOException("Could not logon", logonResponse.getException());
        }
        connectionId = (String) logonResponse.getResult();

        LOGGER.log(Level.FINE, "Connected; connectionId = {0}", connectionId);
        connector.sendConnectionNotification(JMXConnectionNotification.OPENED, connectionId);
    }

    /**
     * Close the connection.
     * @throws IOException if an I/O error occurs when closing the connection
     */
    void close() throws IOException {
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
        if (connectionId != null) {
            // Only send closed notification when we could connect succesfully
            connector.sendConnectionNotification(JMXConnectionNotification.CLOSED, connectionId);
        }
    }

    /**
     * Handle request.
     * @param request request
     * @return response
     * @throws IOException if an I/O error occurs when handling the request
     */
    Response handleRequest(final Request request) throws IOException {
        if (!clientListenerThread.isAlive()) {
            throw new IOException("Listener not running");
        }
        final ClientListener.FutureResponse waiter = clientListener.registerRequest(request);
        send(request);
        return waiter.getResponse();
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
     * @param notificationListener the listener to request
     * @param handback handback given back to the listener
     * @return unique identification for notification listener
     */
    String registerNotificationListener(final NotificationListener notificationListener, final Object handback) {
        return clientListener.registerNotificationListener(notificationListener, handback);
    }

    /**
     * Remove a notification listener.
     * @param notificationListenerId the unique identification of the notifcation listener
     */
    void removeNotificationListener(final String notificationListenerId) {
        clientListener.removeNotificationListener(notificationListenerId);
    }

}
