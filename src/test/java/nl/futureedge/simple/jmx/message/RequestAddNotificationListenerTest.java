package nl.futureedge.simple.jmx.message;

import javax.management.MalformedObjectNameException;
import javax.management.Notification;
import javax.management.NotificationFilter;
import javax.management.ObjectName;
import org.junit.Assert;
import org.junit.Test;

public class RequestAddNotificationListenerTest {

    @Test
    public void test() throws MalformedObjectNameException {
        final String notificationListenerId = "NotificationListenerId";
        final ObjectName name = new ObjectName("nl.futureedge.simple.test:type=Test");
        final NotificationFilter filter = new NotificationFilter() {
            private static final long serialVersionUID = 1L;

            @Override
            public boolean isNotificationEnabled(final Notification notification) {
                return true;
            }
        };
        final RequestAddNotificationListener subject = new RequestAddNotificationListener(notificationListenerId, name, filter);

        Assert.assertEquals(notificationListenerId, subject.getNotificationListenerId());
        Assert.assertEquals(name, subject.getName());
        Assert.assertEquals(filter, subject.getFilter());

        Assert.assertNotNull(subject.toString());
    }
}
