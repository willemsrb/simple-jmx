package nl.futureedge.simple.jmx.authenticator;

import javax.management.remote.JMXPrincipal;
import javax.security.auth.Subject;
import org.junit.Assert;
import org.junit.Test;

public class StaticAuthenticatorTest {

    @Test
    public void test() {
        StaticAuthenticator authenticator = new StaticAuthenticator();
        Subject subject = authenticator.authenticate(null);
        Assert.assertNotNull(subject);
        Assert.assertEquals(0, subject.getPrincipals().size());
    }

    @Test
    public void testWithPrincipal() {
        StaticAuthenticator authenticator = new StaticAuthenticator(new JMXPrincipal("roleA"), new JMXPrincipal("roleB"));
        Subject subject = authenticator.authenticate(new String[] { "dont", "care"});
        Assert.assertNotNull(subject);
        Assert.assertEquals(2, subject.getPrincipals().size());
    }

}
