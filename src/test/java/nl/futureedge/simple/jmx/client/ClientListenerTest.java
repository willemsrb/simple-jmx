package nl.futureedge.simple.jmx.client;


import java.io.IOException;
import java.util.UUID;
import java.util.logging.Level;
import java.util.logging.LogManager;
import javax.management.NotificationListener;
import nl.futureedge.simple.jmx.message.Notification;
import nl.futureedge.simple.jmx.message.RequestLogoff;
import nl.futureedge.simple.jmx.message.RequestLogon;
import nl.futureedge.simple.jmx.message.Response;
import nl.futureedge.simple.jmx.stream.MessageInputStream;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.bridge.SLF4JBridgeHandler;

public class ClientListenerTest {

    static {
        // Configure java.util.logging to log via SLF4j
        SLF4JBridgeHandler.removeHandlersForRootLogger();
        SLF4JBridgeHandler.install();
        LogManager.getLogManager().getLogger("").setLevel(Level.FINEST);
    }

    private final TestInputStream input = new TestInputStream();
    private final ClientListener subject = new ClientListener(new MessageInputStream(input));
    private final Thread subjectThread = new Thread(subject);

    @Before
    public void setup() {
        subjectThread.start();
    }

    @After
    public void destroy() {
        if (subjectThread != null && subjectThread.isAlive()) {
            subject.stop();
            Assert.fail("Tests should stop and cleanup the subject thread!");
        }
    }

    @Test
    public void test() throws InterruptedException, IOException {
        // Response
        final RequestLogon request1 = new RequestLogon(new String[]{"username", "password"});
        final ClientListener.FutureResponse waiter1 = subject.registerRequest(request1);
        input.registerMessage(new Response(request1.getRequestId(), null));
        Assert.assertNotNull(waiter1.getResponse());

        // Timeout
        final RequestLogoff request2 = new RequestLogoff();
        final ClientListener.FutureResponse waiter2 = subject.registerRequest(request2);
        try {
            waiter2.getResponse();
            Assert.fail("Exception expected");
        } catch (final IOException e) {
            // Expected
        }

        // Unknown request
        input.registerMessage(new Response(UUID.randomUUID().toString(), null));

        // Unsupported message
        input.registerMessage(new RequestLogoff());

        final Counter listener1 = new Counter();
        final Object handback1 = new Object();
        final String listenerId1 = subject.registerNotificationListener(listener1, handback1);

        final Counter listener2 = new Counter();
        final String listenerId2 = subject.registerNotificationListener(listener2, null);
        subject.removeNotificationListener(listenerId2);

        input.registerMessage(new Notification(listenerId1, null));
        input.registerMessage(new Notification(listenerId2, null));

        final RequestLogoff notificationsDoneTrigger = new RequestLogoff();
        final ClientListener.FutureResponse trigger = subject.registerRequest(notificationsDoneTrigger);
        input.registerMessage(new Response(notificationsDoneTrigger.getRequestId(), null));
        trigger.getResponse();

        Assert.assertEquals(1, listener1.count);
        Assert.assertEquals(0, listener2.count);

        input.done();

        subject.stop();

        try {
            subject.registerRequest(new RequestLogoff());
            Assert.fail("Exception expected");
        } catch (final IOException e) {
            // Expected
        }

        Assert.assertTrue(subject.isStopped());
        subjectThread.join(5000);
        Assert.assertTrue(subject.isStopped());
    }

    @Test
    public void testServerSendsGarbage() throws InterruptedException {
        input.registerData(new byte[]{0, 0, 0, 4, 3, 3, 3, 3});

        // Give it a little time to handle te data
        Thread.sleep(500);

        Assert.assertTrue(subject.isStopped());
        subjectThread.join(5000);
        Assert.assertTrue(subject.isStopped());
    }

    @Test
    public void testServerClosesConnection() throws InterruptedException {
        input.done();

        // Give it a little time to handle to handle the closure
        Thread.sleep(500);

        Assert.assertTrue(subject.isStopped());
        subjectThread.join(5000);
        Assert.assertTrue(subject.isStopped());
    }

    private static final class Counter implements NotificationListener {

        int count = 0;

        @Override
        public void handleNotification(final javax.management.Notification notification, final Object handback) {
            count++;
        }
    }

}
