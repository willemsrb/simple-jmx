package nl.futureedge.simple.jmx.client;

import java.io.IOException;
import java.io.InterruptedIOException;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.management.NotificationListener;
import nl.futureedge.simple.jmx.message.Message;
import nl.futureedge.simple.jmx.message.Notification;
import nl.futureedge.simple.jmx.message.Request;
import nl.futureedge.simple.jmx.message.Response;
import nl.futureedge.simple.jmx.stream.MessageInputStream;

/**
 * Listener for client connections.
 */
public final class ClientListener implements Runnable {

    private static final Logger LOGGER = Logger.getLogger(ClientListener.class.getName());

    // TODO: Make timeout configurable
    private static final int TIMEOUT = 3;
    private static final TimeUnit TIMEOUT_TIMEUNIT = TimeUnit.SECONDS;

    private final MessageInputStream input;
    private boolean stop = false;

    // FIXME: Potential area for memory leak (if no response is ever received the request-id and waiter will be registered for ever)
    private final Map<String, ResponseWaiter> requests = new HashMap<>();
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
        notificationListeners.put(notificationListenerId,
                new NotificationListenerData(notificationListener, handback));
        return notificationListenerId;
    }


    /**
     * Remove a notification listener.
     * @param notificationListenerId the unique identification of the notifcation listener
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

    void stop() {
        stop = true;
    }

    boolean isStopped() {
        return stop;
    }

    ResponseWaiter registerRequest(final Request request) throws IOException {
        if(stop) {
            throw new IOException("Client listener is stopped");
        }

        synchronized(requests) {
            final ResponseWaiter result = new ResponseWaiter();
            requests.put(request.getRequestId(), result);
            return result;
        }
    }

    private void handleResponse(final Response receivedResponse) {
        final String requestId = receivedResponse.getRequestId();
        synchronized (requests) {
            final ResponseWaiter waiter = requests.remove(requestId);
            if(waiter == null) {
                LOGGER.log(Level.INFO, "Response received for an unknown request (could be timed out). Ignoring response.");
            } else {
                waiter.registerResponse(receivedResponse);
            }
        }
    }

    private void handleNotification(final Notification receivedNotification) {
        final NotificationListenerData listener = notificationListeners
                .get(receivedNotification.getNotificationListenerId());
        if (listener == null) {
            LOGGER.log(Level.INFO, "Notification received for an unknown notification listener. Ignoring notification.");
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
     * Response waiter.
     */
    static final class ResponseWaiter {

        private final CountDownLatch latch = new CountDownLatch(1);
        private Response response;

        void registerResponse(final Response response) {
            this.response = response;
            latch.countDown();
        }

        Response getResponse() throws IOException {
            try {
                if(latch.await(TIMEOUT, TIMEOUT_TIMEUNIT)) {
                    return response;
                } else {
                    throw new IOException("Response not received within timeout period");
                }
            } catch (InterruptedException e) {
                throw new InterruptedIOException("Response wait interrupted");
            }
        }
    }
}
