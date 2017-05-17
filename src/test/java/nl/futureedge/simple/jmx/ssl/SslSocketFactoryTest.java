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
        SslSocketFactory factory = new SslSocketFactory();
        ServerSocket serverSocket = factory.createServerSocket(new JMXServiceURL("simple", "localhost", 0));
        factory.createSocket(new JMXServiceURL("simple", "localhost", serverSocket.getLocalPort()));
    }

    /**
     * Evil reflection.
     */
    @Test
    public void invalidTlsProtocol() throws IOException, ReflectiveOperationException {
        Field allowedProtocols = SslSocketFactory.class.getDeclaredField("TLS_ALLOWED_PROTOCOLS");
        allowedProtocols.setAccessible(true);

        Field allowedProtocolsModifiers = Field.class.getDeclaredField("modifiers");
        allowedProtocolsModifiers.setAccessible(true);
        allowedProtocolsModifiers.setInt(allowedProtocols, allowedProtocols.getModifiers() & ~Modifier.FINAL);

        String[] originalValue = (String[])allowedProtocols.get(null);

        try {
            allowedProtocols.set(null, new String[]{"INVALIDv5.7",});
            try {
                new SslSocketFactory();
                Assert.fail("SslConfigurationException expected");
            } catch (SslSocketFactory.SslConfigurationException e) {
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
        Field allowedCiphersuites = SslSocketFactory.class.getDeclaredField("TLS_ALLOWED_CIPHERSUITES");
        allowedCiphersuites.setAccessible(true);

        Field allowedCiphersuitesModifiers = Field.class.getDeclaredField("modifiers");
        allowedCiphersuitesModifiers.setAccessible(true);
        allowedCiphersuitesModifiers.setInt(allowedCiphersuites, allowedCiphersuites.getModifiers() & ~Modifier.FINAL);

        String[] originalValue = (String[])allowedCiphersuites.get(null);

        try {
            allowedCiphersuites.set(null, new String[]{"TLS_INVALID_WITH_SHA257", "TLS_INVALID_WITH_SHA258",});

            SslSocketFactory factory = new SslSocketFactory();
            factory.createServerSocket(new JMXServiceURL("simple", "localhost", 0));
            // Should not throw an exception, will not work runtime though
        } finally {
            allowedCiphersuites.set(null, originalValue);
        }
    }
}
