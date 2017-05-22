package nl.futureedge.simple.jmx.authenticator;

import javax.security.auth.login.AppConfigurationEntry;
import javax.security.auth.login.Configuration;

/**
 * Base configuration implementation.
 */
class AbstractConfiguration extends Configuration {

    private final String name;
    private final AppConfigurationEntry[] entries;

    /**
     * Create a new configuration.
     * @param name name
     */
    AbstractConfiguration(final String name, final AppConfigurationEntry[] entries) {
        this.name = name;
        this.entries = entries;
    }

    @Override
    public final AppConfigurationEntry[] getAppConfigurationEntry(final String name) {
        if (this.name.equals(name)) {
            return entries;
        } else {
            return null;
        }
    }

}
