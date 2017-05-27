package nl.futureedge.simple.jmx.authenticator;

import java.util.HashMap;
import java.util.Map;
import javax.security.auth.Subject;
import javax.security.auth.callback.UnsupportedCallbackException;
import javax.security.auth.login.LoginException;
import org.junit.Test;

public class PropertiesLoginModuleTest {

    private final PropertiesLoginModule loginModule = new PropertiesLoginModule();
    private final Subject subject = new Subject();
    private final Map<String, Object> sharedState = new HashMap<>();
    private final Map<String, Object> options = new HashMap<>();

    @Test(expected = LoginException.class)
    public void testNoCallbackHandler() throws LoginException {
        loginModule.login(subject, null, sharedState, options);
    }

    @Test(expected = LoginException.class)
    public void testInvalidCallbackHandler() throws LoginException {
        loginModule.login(subject, (callbacks) -> { throw new UnsupportedCallbackException(null, "Failure"); }, sharedState, options);
    }
}
