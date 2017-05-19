package nl.futureedge.simple.jmx.message;

import org.junit.Assert;
import org.junit.Test;

public class RequestLogonTest {

    @Test
    public void test() {
        final RequestLogon subject = new RequestLogon(new String[]{"user", "password"});

        Assert.assertArrayEquals(new String[]{"user", "password"}, (String[]) subject.getCredentials());
        Assert.assertNotNull(subject.toString());
    }
}
