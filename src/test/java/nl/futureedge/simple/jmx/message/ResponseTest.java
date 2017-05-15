package nl.futureedge.simple.jmx.message;

import org.junit.Assert;
import org.junit.Test;

public class ResponseTest {

    @Test
    public void result() {
        final Object result = Integer.valueOf(2);
        final Response subject = new Response("requestId", result);
        Assert.assertEquals("requestId", subject.getRequestId());
        Assert.assertEquals(result, subject.getResult());
        Assert.assertEquals(null, subject.getException());
        Assert.assertEquals("Response [requestId=requestId, result=not null, exception=null]", subject.toString());
    }

    @Test
    public void exception() {
        final Exception exception = new UnsupportedOperationException();
        final Response subject = new Response("requestId", exception);
        Assert.assertEquals("requestId", subject.getRequestId());
        Assert.assertEquals(null, subject.getResult());
        Assert.assertEquals(exception, subject.getException());
        Assert.assertEquals("Response [requestId=requestId, result=null, exception=not null]", subject.toString());
    }

    @Test
    public void nulls() {
        final Response subject = new Response(null, null);
        Assert.assertEquals(null, subject.getRequestId());
        Assert.assertEquals(null, subject.getResult());
        Assert.assertEquals(null, subject.getException());
        Assert.assertEquals("Response [requestId=null, result=null, exception=null]", subject.toString());
    }
}
