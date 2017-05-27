package nl.futureedge.simple.jmx.agent;

import java.util.HashMap;
import java.util.Map;

/**
 * Splits the arguments
 */
final class ArgumentSplitter {

    private ArgumentSplitter() {
        throw new IllegalStateException("Do not instantiate");
    }

    static Map<String, String> split(final String args) {
        final Map<String, String> result = new HashMap<>();

        if (args != null && !"".equals(args.trim())) {
            final String[] parts = args.trim().split(",");
            for (final String part : parts) {
                final int index = part.indexOf('=');
                if (index == -1) {
                    result.put(part.trim(), "");
                } else {
                    result.put(part.substring(0, index).trim(), part.substring(index + 1).trim());
                }
            }
        }

        return result;
    }
}
