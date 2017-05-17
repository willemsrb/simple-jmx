package nl.futureedge.simple.jmx.utils;

import java.io.IOException;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import org.junit.Assert;
import org.junit.Test;

public class IOUtilsTest {

    @Test
    public void closeSilently() {
        IOUtils.closeSilently(this::closeOk);
        IOUtils.closeSilently(this::closeFailure);
    }

    private void closeOk() throws IOException {
        // Nothing
    }

    private void closeFailure() throws IOException {
        throw new IOException("Fail");
    }

    @Test
    public void restrictedConstructor() throws ReflectiveOperationException {
        final Constructor<?> constructor = IOUtils.class.getDeclaredConstructor();
        constructor.setAccessible(true);
        try {
            constructor.newInstance();
            Assert.fail("Constructor should fail");
        } catch (InvocationTargetException e) {
            Assert.assertEquals(IllegalStateException.class, e.getCause().getClass());
        }
    }

}
