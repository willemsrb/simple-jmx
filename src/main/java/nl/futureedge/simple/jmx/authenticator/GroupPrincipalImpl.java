package nl.futureedge.simple.jmx.authenticator;

import java.nio.file.attribute.GroupPrincipal;
import java.util.Objects;

/**
 * Group principal.
 */
final class GroupPrincipalImpl implements GroupPrincipal {

    private final String name;

    /**
     * Create new group principal.
     * @param name name
     */
    GroupPrincipalImpl(final String name) {
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
                && Objects.equals(name, ((GroupPrincipalImpl) that).name);
    }

    @Override
    public String toString() {
        return "GroupPrincipalImpl [name=" + name + "]";
    }

}
