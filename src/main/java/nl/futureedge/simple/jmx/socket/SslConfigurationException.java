package nl.futureedge.simple.jmx.socket;

import javax.management.remote.JMXProviderException;

/**
 * Exception during SSL configuration.
 */
public final class SslConfigurationException extends JMXProviderException {

    private static final long serialVersionUID = 1L;

    /**
     * Create a new exception.
     * @param message message
     */
    SslConfigurationException(final String message) {
        super(message);
    }

    /**
     * Create a new exception.
     * @param message message
     * @param cause cause
     */
    SslConfigurationException(final String message, final Throwable cause) {
        super(message, cause);
    }
}
