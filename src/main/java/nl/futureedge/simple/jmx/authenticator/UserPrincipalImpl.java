package nl.futureedge.simple.jmx.authenticator;

import java.nio.file.attribute.GroupPrincipal;
import java.util.Objects;

/**
 * User principal.
 */
final class UserPrincipalImpl implements GroupPrincipal {

    private final String name;

    /**
     * Create a new user principal.
     * @param name name
     */
    UserPrincipalImpl(final String name) {
        this.name = name;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public int hashCode() {
        return Objects.hash(name);
    }

    @Override
    public boolean equals(final Object that) {
        return that != null && this.getClass() == that.getClass()
                && Objects.equals(name, ((UserPrincipalImpl) that).name);
    }

    @Override
    public String toString() {
        return "UserPrincipalImpl [name=" + name + "]";
    }
}
