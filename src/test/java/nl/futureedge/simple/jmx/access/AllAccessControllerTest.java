package nl.futureedge.simple.jmx.access;

import org.junit.Test;

public class AllAccessControllerTest {

    @Test
    public void test() {
        new AllAccessController().checkAccess(null, null, null);
    }
}
