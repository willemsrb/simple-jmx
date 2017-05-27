package nl.futureedge.simple.jmx.access;

import java.io.IOException;
import java.security.Principal;
import java.util.Collections;
import java.util.HashSet;
import java.util.Properties;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.LogManager;
import javax.management.remote.JMXPrincipal;
import javax.security.auth.Subject;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.bridge.SLF4JBridgeHandler;

public class PropertiesAccessControllerTest {

    static {
        // Configure java.util.logging to log via SLF4j
        SLF4JBridgeHandler.removeHandlersForRootLogger();
        SLF4JBridgeHandler.install();
        LogManager.getLogManager().getLogger("").setLevel(Level.FINEST);
    }

    private final Subject nothing = createSubject("nothing");
    private final Subject reader = createSubject("reader");
    private final Subject writer = createSubject("writer");
    private final Subject creatorSpecific = createSubject("creatorSpecific");
    private final Subject creatorWildcard = createSubject("creatorWildcard");
    private final Subject unregistar = createSubject("unregistar");
    private final Subject admin = createSubject("admin");

    private static Subject createSubject(final String name) {
        final Set<Principal> principals = new HashSet<>();
        principals.add(new JMXPrincipal(name));
        return new Subject(true, principals, Collections.emptySet(), Collections.emptySet());
    }

    private PropertiesAccessController propertiesAccessController;

    @Before
    public void setup() {
        final Properties acl = new Properties();
        acl.put("reader", "readonly");
        acl.put("writer", "readwrite");
        acl.put("creatorSpecific", "readwrite create java.lang.String");
        acl.put("creatorWildcard", "readwrite create java.lang.*");
        acl.put("unregistar", "readwrite unregister");
        acl.put("admin", "readwrite create java.lang.*, java.util.* unregister");

        propertiesAccessController = new PropertiesAccessController(acl);
    }

    private void check(final boolean shouldHaveAccess, final Subject subject, final String methodName, final Object[] parameterValues) {
        try {
            propertiesAccessController.checkAccess(subject, methodName, parameterValues);
            Assert.assertTrue("Should throw a SecurityException", shouldHaveAccess);
        } catch (final SecurityException e) {
            e.printStackTrace();
            Assert.assertFalse("Should not throw a SecurityException", shouldHaveAccess);
        }
    }

    @Test
    public void testRead() {
        final String methodName = "getAttribute";
        final Object[] parameterValues = new Object[]{};

        check(false, nothing, methodName, parameterValues);
        check(true, reader, methodName, parameterValues);
        check(true, writer, methodName, parameterValues);
        check(true, creatorSpecific, methodName, parameterValues);
        check(true, creatorWildcard, methodName, parameterValues);
        check(true, unregistar, methodName, parameterValues);
        check(true, admin, methodName, parameterValues);
    }


    @Test
    public void testWrite() {
        final String methodName = "setAttribute";
        final Object[] parameterValues = new Object[]{};

        check(false, nothing, methodName, parameterValues);
        check(false, reader, methodName, parameterValues);
        check(true, writer, methodName, parameterValues);
        check(true, creatorSpecific, methodName, parameterValues);
        check(true, creatorWildcard, methodName, parameterValues);
        check(true, unregistar, methodName, parameterValues);
        check(true, admin, methodName, parameterValues);
    }

    @Test
    public void testCreateString() {
        final String methodName = "createMBean";
        final Object[] parameterValues = new Object[]{"java.lang.String"};

        check(false, nothing, methodName, parameterValues);
        check(false, reader, methodName, parameterValues);
        check(false, writer, methodName, parameterValues);
        check(true, creatorSpecific, methodName, parameterValues);
        check(true, creatorWildcard, methodName, parameterValues);
        check(false, unregistar, methodName, parameterValues);
        check(true, admin, methodName, parameterValues);
    }

    @Test
    public void testCreateNotString() {
        final String methodName = "createMBean";
        final Object[] parameterValues = new Object[]{"java.lang.Integer"};

        check(false, nothing, methodName, parameterValues);
        check(false, reader, methodName, parameterValues);
        check(false, writer, methodName, parameterValues);
        check(false, creatorSpecific, methodName, parameterValues);
        check(true, creatorWildcard, methodName, parameterValues);
        check(false, unregistar, methodName, parameterValues);
        check(true, admin, methodName, parameterValues);
    }


    @Test
    public void testUnregister() {
        final String methodName = "unregisterMBean";
        final Object[] parameterValues = new Object[]{};

        check(false, nothing, methodName, parameterValues);
        check(false, reader, methodName, parameterValues);
        check(false, writer, methodName, parameterValues);
        check(false, creatorSpecific, methodName, parameterValues);
        check(false, creatorWildcard, methodName, parameterValues);
        check(true, unregistar, methodName, parameterValues);
        check(true, admin, methodName, parameterValues);
    }

    @Test
    public void testCreateNotJavaLang() {
        final String methodName = "createMBean";
        final Object[] parameterValues = new Object[]{"java.util.HashMap"};

        check(false, nothing, methodName, parameterValues);
        check(false, reader, methodName, parameterValues);
        check(false, writer, methodName, parameterValues);
        check(false, creatorSpecific, methodName, parameterValues);
        check(false, creatorWildcard, methodName, parameterValues);
        check(false, unregistar, methodName, parameterValues);
        check(true, admin, methodName, parameterValues);
    }

    @Test
    public void testInvalidMethod() {
        final String methodName = "toString";
        final Object[] parameterValues = new Object[]{};

        check(false, nothing, methodName, parameterValues);
        check(false, reader, methodName, parameterValues);
        check(false, writer, methodName, parameterValues);
        check(false, creatorSpecific, methodName, parameterValues);
        check(false, creatorWildcard, methodName, parameterValues);
        check(false, unregistar, methodName, parameterValues);
        check(false, admin, methodName, parameterValues);
    }

    @Test(expected = SecurityException.class)
    public void testInvalidConfiguration() {
        final Properties acl = new Properties();
        acl.put("reader", "something we dont understand");

        new PropertiesAccessController(acl).checkAccess(reader, "getAttribute", null);
    }

    @Test(expected = SecurityException.class)
    public void testFailingLoader() {
        new PropertiesAccessController(() -> { throw new IOException("Could not load properties"); }).checkAccess(reader, "getAttribute", null);
    }
}
