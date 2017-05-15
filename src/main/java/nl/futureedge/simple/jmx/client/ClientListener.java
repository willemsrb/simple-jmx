package nl.futureedge.simple.jmx.client;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.management.NotificationFilter;
import javax.management.NotificationListener;
import javax.management.ObjectName;

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
	public static final int TIMEOUT = 1000;
	private final MessageInputStream input;
	private boolean stop = false;

	private final Map<String, Response> responses = new HashMap<>();
	private final Map<String, NotificationListenerData> notificationListeners = new HashMap<>();

	/**
	 * Constructor.
	 * 
	 * @param input
	 *            input stream
	 */
	ClientListener(final MessageInputStream input) {
		this.input = input;
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
		final String notificationListenerId = UUID.randomUUID().toString();
		notificationListeners.put(notificationListenerId,
				new NotificationListenerData(objectName, notificationListener, notificationFilter, handback));
		return notificationListenerId;
	}


	/**
	 * Remove a notification listener.
	 * 
	 * @param notificationListenerId
	 *            the unique identification of the notifcation listener
	 */
	public void removeNotificationListener(final String notificationListenerId) {
		notificationListeners.remove(notificationListenerId);
	}

	@Override
	public void run() {
		while (!stop) {
			try {
				LOGGER.log(Level.FINE, "Waiting for object");
				final Object receivedObject = input.read();
				LOGGER.log(Level.FINE, "Received object: {0}", receivedObject);

				if (receivedObject instanceof Response) {
					LOGGER.log(Level.FINE, "Handling response");
					handleResponse((Response) receivedObject);
				} else if (receivedObject instanceof Notification) {
					LOGGER.log(Level.FINE, "Handling notification");
					handleNotification((Notification) receivedObject);
				} else {
					throw new IllegalStateException("Unsupported object received: " + receivedObject);
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

	public void stop() {
		stop = true;
	}

	private void handleResponse(final Response receivedResponse) {
		final String requestId = receivedResponse.getRequestId();
		synchronized (responses) {
			responses.put(requestId, receivedResponse);
			responses.notifyAll();
		}
	}

	Response getResponseFor(final Request request) throws IOException {
		synchronized (responses) {
			while (!responses.containsKey(request.getRequestId())) {
				try {
					responses.wait(TIMEOUT);
					LOGGER.log(Level.FINE, "Checking for response");

					if (stop) {
						throw new IOException("Receiver stopped while waiting for response for request " + request);
					}
				} catch (final InterruptedException e) {
					LOGGER.log(Level.WARNING, "Wait for response interrupted", e);
					Thread.currentThread().interrupt();
					return null;
				}
			}

			return responses.remove(request.getRequestId());
		}
	}

	private void handleNotification(final Notification receivedNotification) {
		final NotificationListenerData listener = notificationListeners
				.get(receivedNotification.getNotificationListenerId());
		if (listener == null) {
			LOGGER.log(Level.WARNING, "Notificatie received for an unknown notification listener");
			return;
		}

		listener.getNotificationListener().handleNotification(receivedNotification.getNotification(),
				listener.getHandback());
	}

	/**
	 * Notification listener data.
	 */
	private static final class NotificationListenerData {
		private final ObjectName objectName;
		private final NotificationListener notificationListener;
		private final NotificationFilter notificationFilter;
		private final Object handback;

		NotificationListenerData(final ObjectName objectName, final NotificationListener notificationListener,
				final NotificationFilter notificationFilter, final Object handback) {
			super();
			this.objectName = objectName;
			this.notificationListener = notificationListener;
			this.notificationFilter = notificationFilter;
			this.handback = handback;
		}

		public ObjectName getObjectName() {
			return objectName;
		}

		public NotificationListener getNotificationListener() {
			return notificationListener;
		}

		public NotificationFilter getNotificationFilter() {
			return notificationFilter;
		}

		public Object getHandback() {
			return handback;
		}
	}

}
