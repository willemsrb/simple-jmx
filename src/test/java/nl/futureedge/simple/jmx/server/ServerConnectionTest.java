package nl.futureedge.simple.jmx.server;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.EOFException;
import java.io.IOException;
import java.io.InputStream;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.LogManager;
import javax.management.InstanceNotFoundException;
import javax.management.JMException;
import javax.management.ListenerNotFoundException;
import javax.management.MBeanServer;
import javax.management.NotificationListener;
import javax.management.ObjectName;
import nl.futureedge.simple.jmx.authenticator.TestAuthenticator;
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
import nl.futureedge.simple.jmx.message.UnknownRequest;
import nl.futureedge.simple.jmx.stream.MessageInputStream;
import nl.futureedge.simple.jmx.stream.MessageOutputStream;
import org.junit.Assert;
import org.junit.Test;
import org.mockito.ArgumentMatchers;
import org.mockito.Mockito;
import org.slf4j.bridge.SLF4JBridgeHandler;

public class ServerConnectionTest {

    static {
        // Configure java.util.logging to log via SLF4j
        SLF4JBridgeHandler.removeHandlersForRootLogger();
        SLF4JBridgeHandler.install();
        LogManager.getLogManager().getLogger("").setLevel(Level.FINEST);
    }

    private Socket socket;
    private ByteArrayOutputStream outputStream;
    private MBeanServer mBeanServer;

    private ServerConnection subject;

    private void setup(final Message... messages) throws IOException {
        final ByteArrayOutputStream buffer = new ByteArrayOutputStream();
        final MessageOutputStream mos = new MessageOutputStream(buffer);
        for (final Message message : messages) {
            mos.write(message);
        }

        setup(new ByteArrayInputStream(buffer.toByteArray()));
    }

    private void setup(final InputStream inputStream) throws IOException {
        outputStream = new ByteArrayOutputStream();

        socket = Mockito.mock(Socket.class);
        Mockito.when(socket.getInputStream()).thenReturn(inputStream);
        Mockito.when(socket.getOutputStream()).thenReturn(outputStream);
        mBeanServer = Mockito.mock(MBeanServer.class);

        subject = new ServerConnection(socket, "connectionId", new TestAuthenticator(), mBeanServer);
    }

    @Test
    public void test() throws IOException, JMException {
        final ObjectName objectName = new ObjectName("test:name=TEST");
        final ObjectName objectName2 = new ObjectName("test:name=TEST2");
        final ObjectName objectName3 = new ObjectName("test:name=TEST3");

        // Requests
        final RequestExecute unauthenticated = new RequestExecute("getAttribute", new Class[]{ObjectName.class, String.class},
                new Object[]{null, null});
        final RequestLogon invalidCredentials = new RequestLogon(new String[]{"unknown", "unknown"});
        final RequestLogon logon = new RequestLogon(new String[]{"admin", "admin"});
        final RequestExecute execute = new RequestExecute("getAttribute", new Class[]{ObjectName.class, String.class},
                new Object[]{objectName, "executeOk"});
        final RequestExecute executeIEWithCause = new RequestExecute("getAttribute", new Class[]{ObjectName.class, String.class},
                new Object[]{objectName, "executeIEWithCause"});
        //        RequestExecute executeIEWithoutCause = new RequestExecute("getAttribute", new Class[]{ObjectName.class, String.class},
        //                new Object[]{objectName, "executeIEWithoutCause"});
        final RequestExecute executeROE = new RequestExecute("getAttribute", new Class[]{ObjectName.class, Integer.class},
                new Object[]{null, null});

        final RequestAddNotificationListener addListener = new RequestAddNotificationListener("id-001", objectName, null);
        final RequestAddNotificationListener addListener2 = new RequestAddNotificationListener("id-002", objectName2, null);
        final RequestAddNotificationListener addListenerOE = new RequestAddNotificationListener("id-003", objectName3, null);

        final RequestExecute triggerNotification = new RequestExecute("getAttribute", new Class[]{ObjectName.class, String.class},
                new Object[]{objectName, "triggerNotification"});

        final Request unknownRequest = new UnknownRequest();
        final Response unknownMessage = new Response("some-id", null);

        final RequestLogoff logoff = new RequestLogoff();

        // Setup
        setup(unauthenticated, invalidCredentials, logon, execute, executeIEWithCause, executeROE, addListener, addListener2, addListenerOE,
                triggerNotification, unknownRequest, unknownMessage, logoff);

        // Mock
        Mockito.when(mBeanServer.getAttribute(objectName, "executeOk")).thenReturn(42);
        Mockito.when(mBeanServer.getAttribute(objectName, "executeIEWithCause")).thenThrow(new IllegalArgumentException());

        final List<NotificationListener> listeners = new ArrayList<>();
        Mockito.doAnswer(invocation -> {
                    listeners.add(invocation.getArgument(1));
                    return null;
                }
        ).when(mBeanServer)
                .addNotificationListener(ArgumentMatchers.eq(objectName), ArgumentMatchers.any(NotificationListener.class), ArgumentMatchers.isNull(),
                        ArgumentMatchers.isNull());

        Mockito.doThrow(new ListenerNotFoundException()
        ).when(mBeanServer).removeNotificationListener(ArgumentMatchers.eq(objectName2), ArgumentMatchers.any(NotificationListener.class));

        Mockito.doThrow(new InstanceNotFoundException()
        ).when(mBeanServer)
                .addNotificationListener(ArgumentMatchers.eq(objectName3), ArgumentMatchers.any(NotificationListener.class), ArgumentMatchers.isNull(),
                        ArgumentMatchers.isNull());

        Mockito.when(mBeanServer.getAttribute(objectName, "triggerNotification")).then(
                invocation -> {
                    listeners.get(0).handleNotification(new javax.management.Notification("type", "source", 1L), null);
                    return null;
                }
        );

        // Execute
        Assert.assertFalse(subject.isStopped());
        subject.run();
        Assert.assertTrue(subject.isStopped());

        // Stopped server connection should not crash on 'late'  notifications
        listeners.get(0).handleNotification(new javax.management.Notification("type", "source", 2L), null);

        // Verify
        final List<Message> messages = readResponses();

        final Response unauthenticatedResponse = (Response) messages.get(0);
        Assert.assertEquals(unauthenticated.getRequestId(), unauthenticatedResponse.getRequestId());
        Assert.assertEquals(NotLoggedOnException.class, unauthenticatedResponse.getException().getClass());

        final Response invalidCredentialsResponse = (Response) messages.get(1);
        Assert.assertEquals(invalidCredentials.getRequestId(), invalidCredentialsResponse.getRequestId());
        Assert.assertEquals(InvalidCredentialsException.class, invalidCredentialsResponse.getException().getClass());

        final Response logonResponse = (Response) messages.get(2);
        Assert.assertEquals(logon.getRequestId(), logonResponse.getRequestId());
        Assert.assertNull(logonResponse.getException());
        Assert.assertEquals("connectionId", logonResponse.getResult());

        final Response executeResponse = (Response) messages.get(3);
        Assert.assertEquals(execute.getRequestId(), executeResponse.getRequestId());
        Assert.assertNull(executeResponse.getException());
        Assert.assertEquals(Integer.valueOf(42), executeResponse.getResult());

        final Response executeIEWithCauseResponse = (Response) messages.get(4);
        Assert.assertEquals(executeIEWithCause.getRequestId(), executeIEWithCauseResponse.getRequestId());
        Assert.assertEquals(IllegalArgumentException.class, executeIEWithCauseResponse.getException().getClass());

        //        Response executeIEWithoutCauseResponse = (Response) messages.get(5);
        //        Assert.assertEquals(executeIEWithoutCause.getRequestId(), executeIEWithoutCauseResponse.getRequestId());
        //        Assert.assertEquals(InvocationTargetException.class, executeIEWithoutCauseResponse.getException().getClass());
        //
        final Response executeROEResponse = (Response) messages.get(5);
        Assert.assertEquals(executeROE.getRequestId(), executeROEResponse.getRequestId());
        Assert.assertEquals(NoSuchMethodException.class, executeROEResponse.getException().getClass());

        final Response addListenerResponse = (Response) messages.get(6);
        Assert.assertEquals(addListener.getRequestId(), addListenerResponse.getRequestId());
        Assert.assertNull(addListenerResponse.getException());

        final Response addListener2Response = (Response) messages.get(7);
        Assert.assertEquals(addListener2.getRequestId(), addListener2Response.getRequestId());
        Assert.assertNull(addListener2Response.getException());

        final Response addListenerOEResponse = (Response) messages.get(8);
        Assert.assertEquals(addListenerOE.getRequestId(), addListenerOEResponse.getRequestId());
        Assert.assertEquals(InstanceNotFoundException.class, addListenerOEResponse.getException().getClass());

        final Notification triggeredNotification = (Notification) messages.get(9);
        Assert.assertEquals(1L, triggeredNotification.getNotification().getSequenceNumber());

        final Response triggerNotificationResponse = (Response) messages.get(10);
        Assert.assertEquals(triggerNotification.getRequestId(), triggerNotificationResponse.getRequestId());
        Assert.assertNull(triggerNotificationResponse.getException());

        final Response unknownRequestResponse = (Response) messages.get(11);
        Assert.assertEquals(unknownRequest.getRequestId(), unknownRequestResponse.getRequestId());
        Assert.assertEquals(UnknownRequestException.class, unknownRequestResponse.getException().getClass());

        final Response logoffResponse = (Response) messages.get(12);
        Assert.assertEquals(logoff.getRequestId(), logoffResponse.getRequestId());

        // Check no more messages
        Assert.assertEquals(13, messages.size());
    }

    @Test
    public void testClientDropsConnectionNoData() throws IOException, JMException {
        setup();
        Assert.assertFalse(subject.isStopped());
        subject.run();
        Assert.assertTrue(subject.isStopped());
    }

    @Test
    public void testClientDropsConnectionDuringLength() throws IOException, JMException {
        final byte[] buffer = new byte[]{0, 0,};
        setup(new ByteArrayInputStream(buffer));

        Assert.assertFalse(subject.isStopped());
        subject.run();
        Assert.assertTrue(subject.isStopped());
    }


    @Test
    public void testClientDropsConnectionDuringData() throws IOException, JMException {
        final byte[] buffer = new byte[]{0, 0, 0, 4, 3, 3};
        setup(new ByteArrayInputStream(buffer));

        Assert.assertFalse(subject.isStopped());
        subject.run();
        Assert.assertTrue(subject.isStopped());
    }


    @Test
    public void testClientSendsGarbage() throws IOException, JMException {
        // 4 bytes length, 4 bytes garbage, 1 byte padding
        final byte[] buffer = new byte[]{0, 0, 0, 4, 3, 3, 3, 3, 3};
        setup(new ByteArrayInputStream(buffer));

        Assert.assertFalse(subject.isStopped());
        subject.run();
        Assert.assertTrue(subject.isStopped());
    }

    @Test
    public void testServerNotificationFailure() throws IOException, JMException {
        final ObjectName objectName = new ObjectName("test:name=TEST");

        final RequestLogon logon = new RequestLogon(new String[]{"admin", "admin"});
        final RequestAddNotificationListener addListener = new RequestAddNotificationListener("id-001", objectName, null);
        final RequestExecute triggerNotification = new RequestExecute("getAttribute", new Class[]{ObjectName.class, String.class},
                new Object[]{objectName, "triggerNotification"});

        setup(logon, addListener, triggerNotification);

        final List<NotificationListener> listeners = new ArrayList<>();
        Mockito.doAnswer(invocation -> {
                    listeners.add(invocation.getArgument(1));
                    return null;
                }
        ).when(mBeanServer)
                .addNotificationListener(ArgumentMatchers.eq(objectName), ArgumentMatchers.any(NotificationListener.class), ArgumentMatchers.isNull(),
                        ArgumentMatchers.isNull());

        Mockito.when(mBeanServer.getAttribute(objectName, "triggerNotification")).then(
                invocation -> {
                    listeners.get(0).handleNotification(new javax.management.Notification("type", new Object(), 1L), null);
                    return null;
                }
        );

        Assert.assertFalse(subject.isStopped());
        subject.run();
        Assert.assertTrue(subject.isStopped());
    }

    private List<Message> readResponses() throws IOException {
        final MessageInputStream inputStream = new MessageInputStream(new ByteArrayInputStream(outputStream.toByteArray()));

        final List<Message> result = new ArrayList<>();
        try {
            while (true) {
                result.add(inputStream.read());
            }
        } catch (final EOFException e) {
            // Expected
        }
        return result;
    }

}
