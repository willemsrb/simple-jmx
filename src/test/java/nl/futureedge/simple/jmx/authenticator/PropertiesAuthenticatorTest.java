package nl.futureedge.simple.jmx.authenticator;


import java.io.IOException;
import java.util.Properties;
import javax.security.auth.Subject;
import nl.futureedge.simple.jmx.utils.PropertiesLoader;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

public class PropertiesAuthenticatorTest {

    private PropertiesAuthenticator authenticator;

    @Before
    public void setup() {
        final Properties passwords = new Properties();
        passwords.put("admin", "admin");

        authenticator = new PropertiesAuthenticator(passwords);
    }

    @Test
    public void test() {
        Subject subject = authenticator.authenticate(new String[]{"admin", "admin"});
        Assert.assertNotNull(subject);
        Assert.assertEquals(1, subject.getPrincipals().size());
        Assert.assertEquals("admin", subject.getPrincipals().iterator().next().getName());
    }

    @Test(expected = SecurityException.class)
    public void testInvalidCredentials() {
        authenticator.authenticate(new String[]{"admin", "invalidPassword"});
    }

    @Test(expected = SecurityException.class)
    public void testNoPropertiesLoader() {
        new PropertiesAuthenticator((PropertiesLoader) null).authenticate(new String[]{"admin", "admin"});
    }

    @Test(expected = SecurityException.class)
    public void testNoPropertiesLoadException() {
        new PropertiesAuthenticator(() -> { throw new IOException("Failure"); }).authenticate(new String[]{"admin", "admin"});
    }

}
