package nl.futureedge.simple.jmx.message;

import org.junit.Assert;
import org.junit.Test;

public class RequestLogoffTest {

    @Test
    public void test() {
        final RequestLogoff subject = new RequestLogoff();
        Assert.assertNotNull(subject.toString());
    }
}
