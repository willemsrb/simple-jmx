package nl.futureedge.simple.jmx.ssl;

import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.net.ServerSocket;
import javax.management.remote.JMXServiceURL;
import org.junit.Assert;
import org.junit.Test;

public class SslSocketFactoryTest {

    @Test
    public void ok() throws IOException {
        final SslSocketFactory factory = new SslSocketFactory();
        final ServerSocket serverSocket = factory.createServerSocket(new JMXServiceURL("simple", "localhost", 0));
        factory.createSocket(new JMXServiceURL("simple", "localhost", serverSocket.getLocalPort()));
    }

    /**
     * Evil reflection.
     */
    @Test
    public void invalidTlsProtocol() throws IOException, ReflectiveOperationException {
        final Field allowedProtocols = SslSocketFactory.class.getDeclaredField("TLS_ALLOWED_PROTOCOLS");
        allowedProtocols.setAccessible(true);

        final Field allowedProtocolsModifiers = Field.class.getDeclaredField("modifiers");
        allowedProtocolsModifiers.setAccessible(true);
        allowedProtocolsModifiers.setInt(allowedProtocols, allowedProtocols.getModifiers() & ~Modifier.FINAL);

        final String[] originalValue = (String[]) allowedProtocols.get(null);

        try {
            allowedProtocols.set(null, new String[]{"INVALIDv5.7",});
            try {
                new SslSocketFactory();
                Assert.fail("SslConfigurationException expected");
            } catch (final SslSocketFactory.SslConfigurationException e) {
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
    public void invalidTlsCiphersuites() throws IOException, ReflectiveOperationException {
        final Field allowedCiphersuites = SslSocketFactory.class.getDeclaredField("TLS_ALLOWED_CIPHERSUITES");
        allowedCiphersuites.setAccessible(true);

        final Field allowedCiphersuitesModifiers = Field.class.getDeclaredField("modifiers");
        allowedCiphersuitesModifiers.setAccessible(true);
        allowedCiphersuitesModifiers.setInt(allowedCiphersuites, allowedCiphersuites.getModifiers() & ~Modifier.FINAL);

        final String[] originalValue = (String[]) allowedCiphersuites.get(null);

        try {
            allowedCiphersuites.set(null, new String[]{"TLS_INVALID_WITH_SHA257", "TLS_INVALID_WITH_SHA258",});

            final SslSocketFactory factory = new SslSocketFactory();
            factory.createServerSocket(new JMXServiceURL("simple", "localhost", 0));
            // Should not throw an exception, will not work runtime though
        } finally {
            allowedCiphersuites.set(null, originalValue);
        }
    }
}
