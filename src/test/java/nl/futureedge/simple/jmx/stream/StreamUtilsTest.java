package nl.futureedge.simple.jmx.stream;

import java.io.IOException;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import nl.futureedge.simple.jmx.message.Message;
import nl.futureedge.simple.jmx.message.RequestLogon;
import org.junit.Assert;
import org.junit.Test;

public class StreamUtilsTest {

    @Test
    public void testLength() throws IOException {
        // One byte
        testLength(50);
        // One byte (negative)
        testLength(238);
        // Two bytes
        testLength(300);
        // Two byte (first negative)
        testLength(500);
        // Two bytes (both negative)
        testLength(65530);
    }

    private void testLength(final int length) throws IOException {
        final byte[] data = StreamUtils.serializeLength(length);
        final int result = StreamUtils.deserializeLength(data);
        Assert.assertEquals(length, result);
    }

    @Test(expected = IOException.class)
    public void invalidLength() throws IOException {
        StreamUtils.deserializeLength(new byte[]{0, 0, 18});
    }

    @Test
    public void testMessage() throws IOException, ReflectiveOperationException {
        testMessage(new RequestLogon("credentialsObject"));
    }

    private void testMessage(final Message message) throws IOException, ReflectiveOperationException {
        final byte[] data = StreamUtils.serializeMessage(message);
        final Message result = StreamUtils.deserializeMessage(data);
        assertEquals(message, result);
    }

    private void assertEquals(final Message expected, final Message actual) throws ReflectiveOperationException {
        Assert.assertEquals(expected.getClass(), actual.getClass());

        for (final Field field : expected.getClass().getDeclaredFields()) {
            field.setAccessible(true);

            final Object expectedFieldValue = field.get(expected);
            final Object actualFieldValue = field.get(actual);
            Assert.assertEquals("Field '" + field.getName() + "' not equal", expectedFieldValue, actualFieldValue);
        }
    }

    @Test
    public void restrictedConstructor() throws ReflectiveOperationException {
        final Constructor<?> constructor = StreamUtils.class.getDeclaredConstructor();
        constructor.setAccessible(true);
        try {
            constructor.newInstance();
            Assert.fail("Constructor should fail");
        } catch (final InvocationTargetException e) {
            Assert.assertEquals(IllegalStateException.class, e.getCause().getClass());
        }
    }

}
