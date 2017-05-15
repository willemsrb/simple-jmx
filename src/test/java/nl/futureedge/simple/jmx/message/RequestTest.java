package nl.futureedge.simple.jmx.message;

import org.junit.Assert;
import org.junit.Test;

public class RequestTest {

    @Test
    public void test() {
        final Request subject = new Request();
        Assert.assertNotNull(subject.getRequestId());
    }

}
