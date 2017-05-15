package nl.futureedge.simple.jmx.stream;

import java.io.IOException;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;

import org.junit.Assert;
import org.junit.Test;

import nl.futureedge.simple.jmx.message.Message;
import nl.futureedge.simple.jmx.message.RequestLogon;

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

	private void testLength(int length) throws IOException {
		final byte[] data = StreamUtils.serializeLength(length);
		final int result = StreamUtils.deserializeLength(data);
		Assert.assertEquals(length, result);
	}

	@Test(expected = IOException.class)
	public void invalidLength() throws IOException {
		StreamUtils.deserializeLength(new byte[] { 0, 0, 18 });
	}

	@Test
	public void testMessage() throws IOException, ReflectiveOperationException {
		testMessage(new RequestLogon("username", "password"));
	}

	private void testMessage(Message message) throws IOException, ReflectiveOperationException {
		final byte[] data = StreamUtils.serializeMessage(message);
		final Message result = StreamUtils.deserializeMessage(data);
		assertEquals(message, result);
	}

	private void assertEquals(final Message expected, final Message actual) throws ReflectiveOperationException {
		Assert.assertEquals(expected.getClass(), actual.getClass());
		
		for(Field field : expected.getClass().getDeclaredFields()) {
			field.setAccessible(true);
			
			Assert.assertEquals("Field '" + field.getName() + "' not equal", field.get(expected), field.get(actual));
		}
	}
	
	@Test
	public void restrictedConstructor() throws ReflectiveOperationException {
		final Constructor<?> constructor = StreamUtils.class.getDeclaredConstructor();
		constructor.setAccessible(true);
		try {
			constructor.newInstance();
			Assert.fail("Constructor should fail");
		}catch(InvocationTargetException e) {
			Assert.assertEquals(IllegalStateException.class, e.getCause().getClass());
		}
	}

}
