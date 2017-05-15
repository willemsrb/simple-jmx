package nl.futureedge.simple.jmx.message;

import org.junit.Assert;
import org.junit.Test;

public class RequestExecuteTest {

    @Test
    public void test() {
        final String methodName = "method";
        final Class<?>[] parameterClasses = new Class[] {String.class, String.class };
        final Object[] parameterValues = new Object[] {"someting", "something" };
        final RequestExecute subject = new RequestExecute(methodName, parameterClasses, parameterValues);

        Assert.assertEquals(methodName, subject.getMethodName());
        Assert.assertArrayEquals(parameterClasses, subject.getParameterClasses());
        Assert.assertArrayEquals(parameterValues, subject.getParameterValues());
        Assert.assertNotNull(subject.toString());
    }

    @Test
    public void testNulls() {
        final RequestExecute subject = new RequestExecute(null, null, null);
        Assert.assertEquals(null, subject.getMethodName());
        Assert.assertArrayEquals(new Class<?>[] {}, subject.getParameterClasses());
        Assert.assertArrayEquals(new Object[] {}, subject.getParameterValues());
        Assert.assertNotNull(subject.toString());
    }
}
