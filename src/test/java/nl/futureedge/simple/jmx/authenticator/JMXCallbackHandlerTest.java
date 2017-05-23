package nl.futureedge.simple.jmx.authenticator;


import javax.security.auth.callback.Callback;
import javax.security.auth.callback.LanguageCallback;
import javax.security.auth.callback.UnsupportedCallbackException;
import javax.security.auth.login.LoginException;
import org.junit.Test;

public class JMXCallbackHandlerTest {

    @Test(expected = LoginException.class)
    public void invalidCredentialsType() throws LoginException {
        new JMXCallbackHandler(new Object());
    }

    @Test(expected = LoginException.class)
    public void invalidCredentialsLength() throws LoginException {
        new JMXCallbackHandler(new String[] { "username"});
    }

    @Test(expected = UnsupportedCallbackException.class)
    public void testUnsupportedCallbackException() throws LoginException, UnsupportedCallbackException {
        JMXCallbackHandler handler = new JMXCallbackHandler(new String[] { "username", "password"});
        Callback callback = new LanguageCallback();
        handler.handle(new Callback[] { callback });
    }
}
