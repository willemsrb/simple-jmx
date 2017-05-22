package nl.futureedge.simple.jmx.access;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

/**
 * {@link javax.management.MBeanServerConnection} methods.
 */
class Methods {

    /**
     * Read methods.
     */
    static final Set<String> READ_METHODS = new HashSet<>(
            Arrays.asList("addNotificationListener", "getAttribute", "getAttributes", "getDefaultDomain", "getDomains",
                    "getMBeanCount", "getMBeanInfo", "getObjectInstance", "isInstanceOf", "isRegistered", "queryMBeans",
                    "queryNames", "removeNotificationListener"));

    /**
     * Write methods.
     */
    static final Set<String> WRITE_METHODS = new HashSet<>(
            Arrays.asList("invoke", "setAttribute", "setAttributes"));

    /**
     * Create methods.
     */
    static final Set<String> CREATE_METHODS = new HashSet<>(Arrays.asList("createMBean"));

    /**
     * Unregister methods.
     */
    static final Set<String> UNREGISTER_METHODS = new HashSet<>(Arrays.asList("unregisterMBean"));

}
