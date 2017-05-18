package nl.futureedge.simple.jmx.server;

import java.io.EOFException;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.management.InstanceNotFoundException;
import javax.management.ListenerNotFoundException;
import javax.management.MBeanServer;
import javax.management.NotificationListener;
import javax.management.ObjectName;
import javax.management.OperationsException;
import nl.futureedge.simple.jmx.exception.InvalidCredentialsException;
import nl.futureedge.simple.jmx.exception.NotLoggedOnException;
import nl.futureedge.simple.jmx.exception.UnknownRequestException;
import nl.futureedge.simple.jmx.message.Message;
import nl.futureedge.simple.jmx.message.Notification;
import nl.futureedge.simple.jmx.message.Request;
import nl.futureedge.simple.jmx.message.RequestAddNotificationListener;
import nl.futureedge.simple.jmx.message.RequestExecute;
import nl.futureedge.simple.jmx.message.RequestLogoff;
import nl.futureedge.simple.jmx.message.RequestLogon;
import nl.futureedge.simple.jmx.message.Response;
import nl.futureedge.simple.jmx.stream.MessageInputStream;
import nl.futureedge.simple.jmx.stream.MessageOutputStream;
import nl.futureedge.simple.jmx.utils.IOUtils;

/**
 * Server connection.
 */
final class ServerConnection implements Runnable {

    private static final Logger LOGGER = Logger.getLogger(ServerConnection.class.getName());

    private final Socket socket;
    private final String connectionId;
    private final MessageInputStream input;
    private final MessageOutputStream output;

    private final MBeanServer mBeanServer;
    private final List<NotificationSender> notificationSenders = new ArrayList<>();

    private boolean authenticated = false;
    private boolean stop = false;

    /**
     * Constructor.
     * @param socket server socket to communicate via
     */
    ServerConnection(final Socket socket, final String connectionId, final MBeanServer mBeanServer) throws IOException {
        this.socket = socket;
        this.connectionId = connectionId;

        // The socket InputStream and OutputStream are not closed directly. They
        // are closed via method calls on the socket itself.
        this.input = new MessageInputStream(socket.getInputStream());
        this.output = new MessageOutputStream(socket.getOutputStream());

        this.mBeanServer = mBeanServer;
    }

    @Override
    public void run() {
        while (!stop) {
            try {
                // Receive request
                final Message message = input.read();

                if(message instanceof Request) {
                    // Handle request
                    final Response response = handleRequest((Request)message);

                    // Send response
                    output.write(response);
                } else {
                    LOGGER.log(Level.WARNING, "Received unknown message type: {0}", message.getClass().getName());
                }
            } catch (EOFException e) {
                LOGGER.log(Level.FINE, "Client closed connection.", e);
                stop = true;
            } catch (IOException e) {
                LOGGER.log(Level.WARNING, "Unexpected exception.", e);
                stop = true;
            }
        }

        LOGGER.log(Level.FINE, "Stopping server connection.");

        // Remove all remote notification listeners we registered
        for (final NotificationSender notificationSender : notificationSenders) {
            try {
                mBeanServer.removeNotificationListener(notificationSender.getObjectName(), notificationSender);
            } catch (final ListenerNotFoundException | InstanceNotFoundException e) {
                LOGGER.log(Level.FINE, "Cleanup of notification listener failed", e);
            }
        }

        // Shutdown
        IOUtils.closeSilently(socket);
        LOGGER.log(Level.FINE, "Server connection closed.");
    }

    public boolean isStopped() {
        return stop;
    }

    private Response handleRequest(final Request request) {
        final Response response;
        if (request instanceof RequestLogon) {
            LOGGER.log(Level.FINE, "Handling logon request");
            response = handleLogon((RequestLogon) request);
            authenticated = response.getException() == null;
            LOGGER.log(Level.FINE, "Logged on: {0}", authenticated);
        } else if (request instanceof RequestLogoff) {
            LOGGER.log(Level.FINE, "Handling logoff request");
            response = new Response(request.getRequestId(), null);
            stop = true;
            LOGGER.log(Level.FINE, "Logged off");
        } else {
            // Check logged on
            if (!authenticated) {
                response = new Response(request.getRequestId(), new NotLoggedOnException());
            } else {
                if (request instanceof RequestExecute) {
                    LOGGER.log(Level.FINE, "Handling execute request: {0}", request);
                    response = handleExecute((RequestExecute) request);
                    LOGGER.log(Level.FINE, "Execute response: {0}", response);
                } else if (request instanceof RequestAddNotificationListener) {
                    LOGGER.log(Level.FINE, "Handling add notification request: {0}", request);
                    response = handleAddNotificationListener((RequestAddNotificationListener) request);
                    LOGGER.log(Level.FINE, "Add notification response: {0}", response);
                } else {
                    LOGGER.log(Level.WARNING, "Received unknown request type: {0}", request.getClass().getName());
                    response = new Response(request.getRequestId(), new UnknownRequestException());
                }
            }
        }
        return response;

    }

    /**
     * Handle logon.
     * @param request request
     * @return response
     */
    private Response handleLogon(final RequestLogon request) {
        // TODO: Add authenticater service
        String username = request.getUsername();
        String password = request.getPassword();

        boolean checkCredentials = "admin".equals(username) && "admin".equals(password);
        if (checkCredentials) {
            return new Response(request.getRequestId(), connectionId);
        } else {
            LOGGER.log(Level.WARNING, "Invalid logon attempt for user '" + request.getUsername() + "'");
            return new Response(request.getRequestId(), new InvalidCredentialsException());
        }
    }

    /**
     * Handle execute.
     * @param request request
     * @return response
     */
    private Response handleExecute(final RequestExecute request) {
        Response response = null;
        try {
            final Method method = MBeanServer.class.getMethod(request.getMethodName(), request.getParameterClasses());
            final Object result = method.invoke(mBeanServer, request.getParameterValues());
            response = new Response(request.getRequestId(), result);
        } catch (final InvocationTargetException e) {
            if (e.getCause() instanceof Exception) {
                response = new Response(request.getRequestId(), (Exception) e.getCause());
            } else {
                response = new Response(request.getRequestId(), e);
            }
        } catch (final ReflectiveOperationException e) {
            response = new Response(request.getRequestId(), e);
        }

        return response;
    }

    /**
     * Handle adding a notification listener.
     * @param request request
     * @return response
     */
    private Response handleAddNotificationListener(final RequestAddNotificationListener request) {
        final NotificationSender notificationSender = new NotificationSender(
                request.getNotificationListenerId(), request.getName());

        try {
            mBeanServer.addNotificationListener(request.getName(), notificationSender, request.getFilter(), null);
            notificationSenders.add(notificationSender);
            return new Response(request.getRequestId(), null);
        } catch (final OperationsException e) {
            return new Response(request.getRequestId(), e);
        }
    }

    /**
     * Notification listener that sends the notification to the client.
     */
    private class NotificationSender implements NotificationListener {

        private final String notificationListenerId;
        private final ObjectName objectName;

        /**
         * Constructor.
         * @param notificationListenerId notification listener id sent with the {@link Notification}
         * @param objectName object name
         */
        public NotificationSender(final String notificationListenerId, final ObjectName objectName) {
            this.notificationListenerId = notificationListenerId;
            this.objectName = objectName;
        }

        /**
         * @return object name
         */
        public ObjectName getObjectName() {
            return objectName;
        }

        @Override
        public void handleNotification(final javax.management.Notification notification, final Object handback) {
            LOGGER.log(Level.FINE, "Received notification");
            if (stop) {
                // Do not sent notifications any more when the server connection
                // is stopped.
                return;
            }
            final Notification response = new Notification(notificationListenerId, notification);
            try {
                LOGGER.log(Level.FINE, "Sending notification");
                output.write(response);
                LOGGER.log(Level.FINE, "Notification sent");
            } catch (final IOException e) {
                LOGGER.log(Level.WARNING,
                        "Unexpected exception during sending notification. Stopping server connection.", e);
                stop = true;
            }
        }
    }

}
