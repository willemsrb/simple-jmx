package nl.futureedge.simple.jmx.access;

import javax.security.auth.Subject;

/**
 * Allow access to all read-only and notification methods.
 *
 * <ul>
 * <li>addNotificationListener</li>
 * <li>getAttribute</li>
 * <li>getAttributes</li>
 * <li>getDefaultDomain</li>
 * <li>getDomains</li>
 * <li>getMBeanCount</li>
 * <li>getMBeanInfo</li>
 * <li>getObjectInstance</li>
 * <li>isInstanceOf</li>
 * <li>isRegistered</li>
 * <li>queryMBeans</li>
 * <li>queryNames</li>
 * <li>removeNotificationListener</li>
 * </ul>
 */
public final class DefaultAccessController implements JMXAccessController {

    @Override
    public void checkAccess(final Subject subject, final String methodName, final Object[] parameterValues) {
        if (!Methods.READ_METHODS.contains(methodName)) {
            throw new SecurityException("Illegal access");
        }
    }

}
