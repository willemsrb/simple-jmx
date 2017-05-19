package nl.futureedge.simple.jmx.authenticator;


import java.security.Principal;
import java.util.HashSet;
import java.util.Set;
import javax.management.remote.JMXAuthenticator;
import javax.security.auth.Subject;

/**
 * Default authenticator.
 *
 * Currently always return an empty Subject.
 */
public class DefaultAuthenticator implements JMXAuthenticator {

    @Override
    public Subject authenticate(final Object credentials) {
        // TODO: support -Dcom.sun.management.jmxremote.password.file=jmxremote.password
        // TODO: Support -Dcom.sun.management.jmxremote.access.file=jmxremote.access (define readonly and readwrite access)

        // The readonly level only allows the JMX client to read an MBean's attributes and receive notifications.
        // The readwrite level also allows setting attributes, invoking operations, and creating and remPrincipalans.
        final Set<Principal> principals = new HashSet<>();
        principals.add(new GroupPrincipalImpl("readonly"));
        return new Subject(true, principals, new HashSet<>(), new HashSet<>());
    }
}
