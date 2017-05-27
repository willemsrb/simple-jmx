package nl.futureedge.simple.jmx.authenticator;

import javax.management.remote.JMXPrincipal;
import javax.security.auth.Subject;
import org.junit.Assert;
import org.junit.Test;

public class StaticAuthenticatorTest {

    @Test
    public void test() {
        final StaticAuthenticator authenticator = new StaticAuthenticator();
        final Subject subject = authenticator.authenticate(null);
        Assert.assertNotNull(subject);
        Assert.assertEquals(0, subject.getPrincipals().size());
    }

    @Test
    public void testWithPrincipal() {
        final StaticAuthenticator authenticator = new StaticAuthenticator(new JMXPrincipal("roleA"), new JMXPrincipal("roleB"));
        final Subject subject = authenticator.authenticate(new String[]{"dont", "care"});
        Assert.assertNotNull(subject);
        Assert.assertEquals(2, subject.getPrincipals().size());
    }

}
