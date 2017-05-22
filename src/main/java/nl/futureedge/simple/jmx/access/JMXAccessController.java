package nl.futureedge.simple.jmx.access;

import javax.security.auth.Subject;

/**
 * Control access.
 */
@FunctionalInterface
public interface JMXAccessController {

    /**
     * Check access for a specific invocation on the mbean server.
     * @param subject subject
     * @param methodName method name
     * @param parameterValues parameter values
     */
    void checkAccess(Subject subject, String methodName, Object[] parameterValues);
}
