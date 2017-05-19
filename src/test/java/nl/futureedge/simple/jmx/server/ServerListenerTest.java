package nl.futureedge.simple.jmx.server;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.reflect.Field;
import java.net.ServerSocket;
import java.net.SocketException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;
import java.util.logging.Level;
import java.util.logging.LogManager;
import javax.management.MBeanServer;
import javax.management.remote.JMXServiceURL;
import javax.net.ssl.SSLSocket;
import nl.futureedge.simple.jmx.ssl.SslSocketFactory;
import org.awaitility.Awaitility;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;
import org.slf4j.bridge.SLF4JBridgeHandler;

public class ServerListenerTest {

    static {
        // Configure java.util.logging to log via SLF4j
        SLF4JBridgeHandler.removeHandlersForRootLogger();
        SLF4JBridgeHandler.install();
        LogManager.getLogManager().getLogger("").setLevel(Level.FINEST);
    }

    private MBeanServer mBeanServer;
    private ServerConnector serverConnector;
    private ServerListener subject;
    private Thread subjectThread;

    @Before
    public void setup() throws IOException {
        mBeanServer = Mockito.mock(MBeanServer.class);
        serverConnector = new ServerConnector(new JMXServiceURL("simple", "localhost", 0), null, mBeanServer);
        subject = new ServerListener(serverConnector, null);
    }

    private void start() {
        subjectThread = new Thread(subject);
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
    public void test() throws IOException, InterruptedException {
        Assert.assertNotNull(subject.getServerId());
        Assert.assertFalse(subject.isStopped());

        start();

        final SSLSocket client = (SSLSocket) new SslSocketFactory().createSocket(serverConnector.getAddress());
        client.startHandshake();
        Assert.assertTrue(client.isConnected());
        final OutputStream clientOutput = client.getOutputStream();
        final InputStream clientInput = client.getInputStream();
        client.close();
        Assert.assertFalse(subject.isStopped());

        subject.stop();
        Assert.assertTrue(subject.isStopped());
        subjectThread.join(5000);
        Assert.assertTrue(subject.isStopped());
    }

    @Test
    public void testUnexpectedSocketException() throws ReflectiveOperationException, IOException, InterruptedException {
        final ServerSocket mockSocket = Mockito.mock(ServerSocket.class);

        final Field socketField = ServerListener.class.getDeclaredField("serverSocket");
        socketField.setAccessible(true);
        socketField.set(subject, mockSocket);

        AtomicLong counter = new AtomicLong(0);
        Mockito.when(mockSocket.accept()).thenAnswer(invocation -> {
            counter.getAndIncrement();
            throw new SocketException();
        });

        start();
        Assert.assertFalse(subject.isStopped());

        // Let it fail a couple of time
        Awaitility.await().atMost(3, TimeUnit.SECONDS).until(() -> counter.get() > 5);
        Assert.assertFalse(subject.isStopped());

        subject.stop();
        Assert.assertTrue(subject.isStopped());
        subjectThread.join(5000);
        Assert.assertTrue(subject.isStopped());
    }


    @Test
    public void testUnexpectedIOException() throws ReflectiveOperationException, IOException, InterruptedException {
        final ServerSocket mockSocket = Mockito.mock(ServerSocket.class);

        final Field socketField = ServerListener.class.getDeclaredField("serverSocket");
        socketField.setAccessible(true);
        socketField.set(subject, mockSocket);

        AtomicLong counter = new AtomicLong(0);
        Mockito.when(mockSocket.accept()).thenAnswer(invocation -> {
            counter.getAndIncrement();
            throw new IOException();
        });

        start();
        Assert.assertFalse(subject.isStopped());

        // Let it fail a couple of time
        Awaitility.await().atMost(3, TimeUnit.SECONDS).until(() -> counter.get() > 5);
        Assert.assertFalse(subject.isStopped());

        subject.stop();
        Assert.assertTrue(subject.isStopped());
        subjectThread.join(5000);
        Assert.assertTrue(subject.isStopped());
    }
}
