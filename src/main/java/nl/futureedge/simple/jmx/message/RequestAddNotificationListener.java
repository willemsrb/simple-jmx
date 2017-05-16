package nl.futureedge.simple.jmx.message;

import javax.management.NotificationFilter;
import javax.management.ObjectName;

/**
 * Request to add a notification listener.
 */
public final class RequestAddNotificationListener extends Request {

    private static final long serialVersionUID = 1L;

    private final String notificationListenerId;
    private final ObjectName name;
    private final NotificationFilter filter;

    /**
     * Constructor.
     * @param notificationListenerId unique notification listener identification
     * @param name mbean to register the notification listener for
     * @param filter notification filter
     */
    public RequestAddNotificationListener(final String notificationListenerId, final ObjectName name,
                                          final NotificationFilter filter) {
        this.notificationListenerId = notificationListenerId;
        this.name = name;
        this.filter = filter;
    }

    public String getNotificationListenerId() {
        return notificationListenerId;
    }

    public ObjectName getName() {
        return name;
    }

    public NotificationFilter getFilter() {
        return filter;
    }

    @Override
    public String toString() {
        return "RequestAddNotificationListener [requestId=" + getRequestId() + ", notificationListenerId="
                + notificationListenerId + ", name=" + name + ", filter=" + filter + "]";
    }
}
