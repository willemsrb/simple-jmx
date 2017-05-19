package nl.futureedge.simple.jmx.client;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.management.NotificationListener;
import nl.futureedge.simple.jmx.exception.RequestTimedOutException;
import nl.futureedge.simple.jmx.message.Message;
import nl.futureedge.simple.jmx.message.Notification;
import nl.futureedge.simple.jmx.message.Request;
import nl.futureedge.simple.jmx.message.Response;
import nl.futureedge.simple.jmx.stream.MessageInputStream;

/**
 * Listener for client connections.
 */
final class ClientListener implements Runnable {

    private static final Logger LOGGER = Logger.getLogger(ClientListener.class.getName());

    // TODO: Make timeout configurable
    private static final int TIMEOUT = 3;

    private final MessageInputStream input;
    private boolean stop = false;

    // FIXME: Potential area for memory leak
    // if no response is ever received the request-id and waiter will be registered for ever)
    private final Map<String, FutureResponse> requests = new HashMap<>();
    private final Map<String, NotificationListenerData> notificationListeners = new HashMap<>();

    /**
     * Constructor.
     * @param input input stream
     */
    ClientListener(final MessageInputStream input) {
        this.input = input;
    }

    /**
     * Register a notification listener.
     * @param notificationListener the listener to request
     * @param handback handback given back to the listener
     * @return unique identification for notification listener
     */
    String registerNotificationListener(final NotificationListener notificationListener, final Object handback) {
        final String notificationListenerId = UUID.randomUUID().toString();
        notificationListeners.put(notificationListenerId, new NotificationListenerData(notificationListener, handback));
        return notificationListenerId;
    }

    /**
     * Remove a notification listener.
     * @param notificationListenerId the unique identification of the notification listener
     */
    void removeNotificationListener(final String notificationListenerId) {
        notificationListeners.remove(notificationListenerId);
    }

    @Override
    public void run() {
        while (!stop) {
            try {
                LOGGER.log(Level.FINE, "Waiting for message");
                final Message message = input.read();
                LOGGER.log(Level.FINE, "Received message: {0}", message);

                if (message instanceof Response) {
                    LOGGER.log(Level.FINE, "Handling response");
                    handleResponse((Response) message);
                } else if (message instanceof Notification) {
                    LOGGER.log(Level.FINE, "Handling notification");
                    handleNotification((Notification) message);
                } else {
                    LOGGER.log(Level.WARNING, "Received unknown message type: {0}", message.getClass().getName());
                }
            } catch (final IOException e) {
                // When stopping we need to close the socket; that results in an
                // IOException from the inputstream.read which we don't count as
                // an error we need to report
                if (!stop) {
                    LOGGER.log(Level.SEVERE, "Unexpected exception when handling response/notification", e);
                    stop = true;
                }
            }
        }
    }

    /**
     * Stop the listener.
     */
    void stop() {
        stop = true;
    }

    /**
     * Is the listener stopped? A listener is stopped when the {@link #stop}
     * method has been called, or when a exception has occurred that the
     * listener cannot recover from.
     * @return true, if the listener has stopped, else false
     */
    boolean isStopped() {
        return stop;
    }

    /**
     * Register a new request and returns the 'future' (functions like a Future
     * but cannot be cancelled) to retrieve the result.
     * @param request request
     * @return future result
     * @throws IOException if an I/O exception occurs when registering the request
     */
    FutureResponse registerRequest(final Request request) throws IOException {
        if (stop) {
            throw new IOException("Client listener is stopped");
        }

        synchronized (requests) {
            final FutureResponse result = new FutureResponse();
            requests.put(request.getRequestId(), result);
            return result;
        }
    }

    private void handleResponse(final Response receivedResponse) {
        final String requestId = receivedResponse.getRequestId();
        synchronized (requests) {
            final FutureResponse waiter = requests.remove(requestId);
            if (waiter == null) {
                LOGGER.log(Level.INFO,
                        "Response received for an unknown request (could be timed out). Ignoring response.");
            } else {
                waiter.registerResponse(receivedResponse);
            }
        }
    }

    private void handleNotification(final Notification receivedNotification) {
        final NotificationListenerData listener = notificationListeners
                .get(receivedNotification.getNotificationListenerId());
        if (listener == null) {
            LOGGER.log(Level.INFO,
                    "Notification received for an unknown notification listener. Ignoring notification.");
            return;
        }

        listener.getNotificationListener().handleNotification(receivedNotification.getNotification(),
                listener.getHandback());
    }

    /**
     * Notification listener data.
     */
    private static final class NotificationListenerData {
        private final NotificationListener notificationListener;
        private final Object handback;

        NotificationListenerData(final NotificationListener notificationListener, final Object handback) {
            this.notificationListener = notificationListener;
            this.handback = handback;
        }

        NotificationListener getNotificationListener() {
            return notificationListener;
        }

        Object getHandback() {
            return handback;
        }
    }

    /**
     * Future response.
     */
    static final class FutureResponse {

        private final CountDownLatch latch = new CountDownLatch(1);
        private Response response;

        /**
         * Signal that the response has been received.
         * @param response response
         */
        void registerResponse(final Response response) {
            this.response = response;
            latch.countDown();
        }

        /**
         * Return the response, blocking until it has been received.
         * @return the response
         * @throws IOException if an error occurs when waiting for the response (timeout or interrupted)
         */
        Response getResponse() throws IOException {
            try {
                if (latch.await(TIMEOUT, TimeUnit.SECONDS)) {
                    return response;
                } else {
                    throw new RequestTimedOutException();
                }
            } catch (final InterruptedException e) {
                Thread.currentThread().interrupt();
                return null;
            }
        }
    }
}
