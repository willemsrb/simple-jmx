package nl.futureedge.simple.jmx.server;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import org.junit.Assert;
import org.junit.Test;

public class EnvironmentTest {

    @Test
    public void restrictedConstructor() throws ReflectiveOperationException {
        final Constructor<?> constructor = Environment.class.getDeclaredConstructor();
        constructor.setAccessible(true);
        try {
            constructor.newInstance();
            Assert.fail("Constructor should fail");
        } catch (final InvocationTargetException e) {
            Assert.assertEquals(IllegalStateException.class, e.getCause().getClass());
        }
    }

}
