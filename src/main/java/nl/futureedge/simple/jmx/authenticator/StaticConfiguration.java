package nl.futureedge.simple.jmx.authenticator;

import java.security.Principal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.security.auth.login.AppConfigurationEntry;

/**
 * Static configuration.
 *
 * Checks nothing, always returns the configured principals.
 */
final class StaticConfiguration extends AbstractConfiguration {

    static final String PRINCIPALS = "principals";

    /**
     * Create a new configuration.
     * @param name name
     */
    StaticConfiguration(final String name, final Principal... principals) {
        super(name, createEntries(principals));
    }

    private static AppConfigurationEntry[] createEntries(final Principal... principals) {
        final Map<String, Object> options = new HashMap<>();
        final List<Principal> principalsOption = new ArrayList<>();
        principalsOption.addAll(Arrays.asList(principals));
        options.put(PRINCIPALS, principalsOption);

        return new AppConfigurationEntry[]{new AppConfigurationEntry(StaticLoginModule.class.getName(),
                AppConfigurationEntry.LoginModuleControlFlag.REQUIRED, options),};
    }
}
