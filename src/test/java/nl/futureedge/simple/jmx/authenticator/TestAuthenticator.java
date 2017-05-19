package nl.futureedge.simple.jmx.authenticator;

import java.security.Principal;
import java.util.HashSet;
import java.util.Set;
import javax.management.remote.JMXAuthenticator;
import javax.security.auth.Subject;

public class TestAuthenticator implements JMXAuthenticator {

    @Override
    public Subject authenticate(final Object credentials) {
        if (credentials instanceof String[] && ((String[]) credentials).length == 2
                && "admin".equals(((String[]) credentials)[0]) && "admin".equals(((String[]) credentials)[1])) {
            final Set<Principal> principals = new HashSet<>();
            principals.add(new UserPrincipalImpl("admin"));
            principals.add(new GroupPrincipalImpl("readwrite"));
            return new Subject(true, principals, new HashSet<>(), new HashSet<>());
        } else {
            throw new SecurityException("Invalid credentials");
        }
    }

}
