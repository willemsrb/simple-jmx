package nl.futureedge.simple.jmx.authenticator;

import java.security.Principal;
import java.util.List;
import java.util.Map;
import javax.security.auth.Subject;
import javax.security.auth.callback.CallbackHandler;
import javax.security.auth.login.LoginException;

/**
 * Static login module.
 */
public final class StaticLoginModule extends AbstractLoginModule {

    /**
     * Create a new static login module.
     */
    public StaticLoginModule() {
    }

    /**
     * @return the principals configured in the {@link StaticConfiguration}
     */
    @SuppressWarnings("unchecked")
    @Override
    protected List<Principal> login(final Subject subject, final CallbackHandler callbackHandler,
                                    final Map<String, ?> sharedState, final Map<String, ?> options) throws LoginException {
        return (List<Principal>) options.get(StaticConfiguration.PRINCIPALS);
    }

}
