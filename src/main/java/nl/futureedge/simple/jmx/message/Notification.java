package nl.futureedge.simple.jmx.message;

/**
 * Notification, initiated by the server, sent to the client.
 */
public final class Notification implements Message {

    private static final long serialVersionUID = 1L;

    private final String notificationListenerId;

    private final javax.management.Notification theNotification;

    /**
     * Constructor.
     * @param notificationListenerId unique notification listener identification to identify the listener this notification is for
     * @param theNotification notification
     */
    public Notification(final String notificationListenerId, final javax.management.Notification theNotification) {
        this.notificationListenerId = notificationListenerId;
        this.theNotification = theNotification;
    }

    public String getNotificationListenerId() {
        return notificationListenerId;
    }

    public javax.management.Notification getNotification() {
        return theNotification;
    }

    @Override
    public String toString() {
        return "Notification [notificationListenerId=" + notificationListenerId + ", theNotification=" + theNotification + "]";
    }
}
