package nl.futureedge.simple.jmx.message;

import org.junit.Assert;
import org.junit.Test;

public class RequestLogonTest {

    @Test
    public void test() {
        final RequestLogon subject = new RequestLogon("user", "password");

        Assert.assertEquals("user", subject.getUsername());
        Assert.assertEquals("password", subject.getPassword());
        Assert.assertNotNull(subject.toString());
    }
}
