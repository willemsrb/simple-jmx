package nl.futureedge.simple.jmx.access;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import org.junit.Assert;
import org.junit.Test;

public class MethodsTest {

    @Test
    public void restrictedConstructor() throws ReflectiveOperationException {
        final Constructor<?> constructor = Methods.class.getDeclaredConstructor();
        constructor.setAccessible(true);
        try {
            constructor.newInstance();
            Assert.fail("Constructor should fail");
        } catch (final InvocationTargetException e) {
            Assert.assertEquals(IllegalStateException.class, e.getCause().getClass());
        }
    }

}
