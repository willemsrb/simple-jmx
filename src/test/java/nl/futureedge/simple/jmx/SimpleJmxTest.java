package nl.futureedge.simple.jmx;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;

import org.junit.Assert;
import org.junit.Test;

public class SimpleJmxTest {

	@Test
	public void test() {
		Assert.assertEquals("simple", SimpleJmx.PROTOCOL);
	}

	@Test
	public void restrictedConstructor() throws ReflectiveOperationException {
		final Constructor<?> constructor = SimpleJmx.class.getDeclaredConstructor();
		constructor.setAccessible(true);
		try {
			constructor.newInstance();
			Assert.fail("Constructor should fail");
		}catch(InvocationTargetException e) {
			Assert.assertEquals(IllegalStateException.class, e.getCause().getClass());
		}
	}
}
