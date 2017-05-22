package nl.futureedge.simple.jmx.authenticator;

import java.util.HashMap;
import java.util.Map;
import javax.security.auth.login.AppConfigurationEntry;
import nl.futureedge.simple.jmx.utils.PropertiesLoader;

/**
 * Property file based configuration.
 *
 * Checks nothing, always returns the configured principals.
 */
final class PropertiesConfiguration extends AbstractConfiguration {

    static final String PROPERTIES_LOADER = "propertiesLoader";

    /**
     * Create a new configuration.
     * @param name name
     */
    PropertiesConfiguration(final String name, final PropertiesLoader propertiesLoader) {
        super(name, createEntries(propertiesLoader));
    }

    private static AppConfigurationEntry[] createEntries(final PropertiesLoader propertiesLoader) {
        final Map<String, Object> options = new HashMap<>();
        options.put(PROPERTIES_LOADER, propertiesLoader);

        return new AppConfigurationEntry[]{new AppConfigurationEntry(PropertiesLoginModule.class.getName(),
                AppConfigurationEntry.LoginModuleControlFlag.REQUIRED, options),};
    }
}
