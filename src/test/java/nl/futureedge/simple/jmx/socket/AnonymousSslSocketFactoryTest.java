package nl.futureedge.simple.jmx.socket;

import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.net.ServerSocket;
import javax.management.remote.JMXServiceURL;
import org.junit.Assert;
import org.junit.Test;

public class AnonymousSslSocketFactoryTest {

    @Test
    public void ok() throws IOException {
        final AnonymousSslSocketFactory factory = new AnonymousSslSocketFactory();
        final ServerSocket serverSocket = factory.createServerSocket(new JMXServiceURL("simple", "localhost", 0));
        factory.createSocket(new JMXServiceURL("simple", "localhost", serverSocket.getLocalPort()));
    }

    /**
     * Evil reflection.
     */
    @Test
    public void invalidTlsProtocol() throws IOException, ReflectiveOperationException {
        final Field allowedProtocols = AnonymousSslSocketFactory.class.getDeclaredField("TLS_ALLOWED_PROTOCOLS");
        allowedProtocols.setAccessible(true);

        final Field allowedProtocolsModifiers = Field.class.getDeclaredField("modifiers");
        allowedProtocolsModifiers.setAccessible(true);
        allowedProtocolsModifiers.setInt(allowedProtocols, allowedProtocols.getModifiers() & ~Modifier.FINAL);

        final String[] originalValue = (String[]) allowedProtocols.get(null);

        try {
            allowedProtocols.set(null, new String[]{"INVALIDv5.7",});
            try {
                new AnonymousSslSocketFactory();
                Assert.fail("SslConfigurationException expected");
            } catch (final SslConfigurationException e) {
                // Expected
            }
        } finally {
            allowedProtocols.set(null, originalValue);
        }
    }


    /**
     * Evil reflection.
     */
    @Test
    public void invalidAnonymousCiphersuites() throws IOException, ReflectiveOperationException {
        final Field allowedCiphersuites = AnonymousSslSocketFactory.class.getDeclaredField("ANONYMOUS_CIPHERSUITES");
        allowedCiphersuites.setAccessible(true);

        final Field allowedCiphersuitesModifiers = Field.class.getDeclaredField("modifiers");
        allowedCiphersuitesModifiers.setAccessible(true);
        allowedCiphersuitesModifiers.setInt(allowedCiphersuites, allowedCiphersuites.getModifiers() & ~Modifier.FINAL);

        final String[] originalValue = (String[]) allowedCiphersuites.get(null);

        try {
            allowedCiphersuites.set(null, new String[]{"TLS_INVALID_WITH_SHA257", "TLS_INVALID_WITH_SHA258",});

            try {
                new AnonymousSslSocketFactory();
                Assert.fail("SslConfigurationException expected");
            } catch (final SslConfigurationException e) {
                // Expected
            }
        } finally {
            allowedCiphersuites.set(null, originalValue);
        }
    }
}
