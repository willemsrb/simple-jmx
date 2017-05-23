package nl.futureedge.simple.jmx.access;

import org.junit.Test;

public class DefaultAccessControllerTest {

    @Test
    public void test() {
        new DefaultAccessController().checkAccess(null, "getAttribute", null);
    }

    @Test(expected=SecurityException.class)
    public void testNoAccess() {
        new DefaultAccessController().checkAccess(null, "invoke", null);
    }
}
