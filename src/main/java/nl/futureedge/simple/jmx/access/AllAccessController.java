package nl.futureedge.simple.jmx.access;

import javax.security.auth.Subject;

/**
 * Allow access to everything.
 */
public final class AllAccessController implements JMXAccessController {

    @Override
    public void checkAccess(final Subject subject, final String methodName, final Object[] parameterValues) {
        // Everything is allowed
    }
}
