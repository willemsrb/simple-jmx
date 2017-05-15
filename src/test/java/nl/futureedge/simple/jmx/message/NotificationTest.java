package nl.futureedge.simple.jmx.message;

import org.junit.Assert;
import org.junit.Test;

public class NotificationTest {

    @Test
    public void test() {
        final String notificationListenerId = "42";
        final javax.management.Notification notification = new javax.management.Notification("Test", new Object(), 42L);

        final Notification subject = new Notification(notificationListenerId, notification);

        Assert.assertEquals(notificationListenerId, subject.getNotificationListenerId());
        Assert.assertEquals(notification, subject.getNotification());

        Assert.assertNotNull(subject.toString());
    }
}
