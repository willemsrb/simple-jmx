package nl.futureedge.simple.jmx.ssl;

import java.io.IOException;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.management.remote.JMXProviderException;
import javax.management.remote.JMXServiceURL;
import javax.net.ssl.KeyManager;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLServerSocket;
import javax.net.ssl.SSLSocket;
import javax.net.ssl.TrustManager;

/**
 * SSL Socket factory.
 */
public final class SslSocketFactory {

    private static final int BACKLOG = 50;
    private static final Logger LOGGER = Logger.getLogger(SslSocketFactory.class.getName());

    private static final String[] TLS_ALLOWED_PROTOCOLS = {"TLSv1.2",};

    private static final String[] TLS_ALLOWED_CIPHERSUITES = {"TLS_ECDHE_ECDSA_WITH_AES_256_GCM_SHA384",
            "TLS_ECDHE_ECDSA_WITH_AES_128_GCM_SHA256", "TLS_ECDHE_RSA_WITH_AES_256_GCM_SHA384",
            "TLS_ECDHE_RSA_WITH_AES_128_GCM_SHA256", "TLS_ECDH_ECDSA_WITH_AES_256_GCM_SHA384",
            "TLS_ECDH_ECDSA_WITH_AES_128_GCM_SHA256", "TLS_ECDH_RSA_WITH_AES_256_GCM_SHA384",
            "TLS_ECDH_RSA_WITH_AES_128_GCM_SHA256", "TLS_RSA_WITH_AES_256_GCM_SHA384",
            "TLS_RSA_WITH_AES_128_GCM_SHA256",

            // Allow SSL without server certificate
            "TLS_DH_anon_WITH_AES_128_GCM_SHA256", "TLS_DH_anon_WITH_AES_128_CBC_SHA256",
            "TLS_DH_anon_WITH_AES_128_CBC_SHA",};

    private final SSLContext sslContext;

    /**
     * Constructor.
     * @throws SslConfigurationException if an error occurs when configuring the SSL
     */
    public SslSocketFactory() throws SslConfigurationException {
        try {
            sslContext = SSLContext.getInstance(TLS_ALLOWED_PROTOCOLS[0]);
        } catch (final NoSuchAlgorithmException e) {
            throw new SslConfigurationException("Algorithm '" + TLS_ALLOWED_PROTOCOLS[0] + "' not found", e);
        }
        final KeyManager[] keyManagers = configureKeyManager();
        final TrustManager[] trustManagers = configureTrustManagers();
        try {
            sslContext.init(keyManagers, trustManagers, null);
        } catch (final KeyManagementException e) {
            throw new SslConfigurationException("Unexpected exception initializing SSL context", e);
        }
    }

    private KeyManager[] configureKeyManager() {
        // TODO: Configure certificates
        return new KeyManager[]{};
    }

    private TrustManager[] configureTrustManagers() {
        // TODO: Configure certificates
        return new TrustManager[]{};
    }

    /**
     * Create a client socket.
     * @param serviceUrl jmx service url
     * @return client socket
     * @throws IOException if an I/O error occurs when creating the socket
     */
    public Socket createSocket(final JMXServiceURL serviceUrl) throws IOException {
        final SSLSocket baseSslSocket = (SSLSocket) sslContext.getSocketFactory().createSocket(serviceUrl.getHost(),
                serviceUrl.getPort());
        baseSslSocket.setEnabledProtocols(filter(TLS_ALLOWED_PROTOCOLS, baseSslSocket.getSupportedProtocols()));
        baseSslSocket
                .setEnabledCipherSuites(filter(TLS_ALLOWED_CIPHERSUITES, baseSslSocket.getSupportedCipherSuites()));

        LOGGER.log(Level.FINE, "Created client socket\nEnabled protocols: {0}\nEnabled cipher suites: {1}",
                new Object[]{Arrays.asList(baseSslSocket.getEnabledProtocols()),
                        Arrays.asList(baseSslSocket.getEnabledCipherSuites()),});

        return baseSslSocket;
    }

    /**
     * Create a server socket.
     * @param serviceUrl jmx service url
     * @return server socket
     * @throws IOException if an I/O error occurs when creating the socket
     */
    public ServerSocket createServerSocket(final JMXServiceURL serviceUrl) throws IOException {
        final InetAddress host = InetAddress.getByName(serviceUrl.getHost());
        final SSLServerSocket baseSslServerSocket = (SSLServerSocket) sslContext.getServerSocketFactory()
                .createServerSocket(serviceUrl.getPort(), BACKLOG, host);
        baseSslServerSocket
                .setEnabledProtocols(filter(TLS_ALLOWED_PROTOCOLS, baseSslServerSocket.getSupportedProtocols()));
        baseSslServerSocket.setEnabledCipherSuites(
                filter(TLS_ALLOWED_CIPHERSUITES, baseSslServerSocket.getSupportedCipherSuites()));

        LOGGER.log(Level.FINE, "Created server socket\nEnabled protocols: {0}\nEnabled cipher suites: {1}",
                new Object[]{Arrays.asList(baseSslServerSocket.getEnabledProtocols()),
                        Arrays.asList(baseSslServerSocket.getEnabledCipherSuites()),});

        return baseSslServerSocket;
    }

    private String[] filter(final String[] allAllowed, final String[] allSupported) {
        final List<String> result = new ArrayList<>();
        final List<String> supportedList = Arrays.asList(allSupported);

        for (final String allowed : allAllowed) {
            if (supportedList.contains(allowed)) {
                result.add(allowed);
            }
        }

        if (result.isEmpty()) {
            LOGGER.log(Level.SEVERE, "None of the allowed ({0}) are are supported: {1}",
                    new Object[]{Arrays.asList(allAllowed), supportedList});
        }

        return result.toArray(new String[]{});
    }

    /**
     * Exception during SSL configuration.
     */
    static final class SslConfigurationException extends JMXProviderException {

        private static final long serialVersionUID = 1L;

        /**
         * SSL Confuguratie Exception.
         * @param message String
         * @param cause Eigenlijke Exception
         */
        SslConfigurationException(final String message, final Throwable cause) {
            super(message, cause);
        }
    }

}
