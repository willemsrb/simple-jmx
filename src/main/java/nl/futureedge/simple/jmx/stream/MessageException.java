package nl.futureedge.simple.jmx.stream;

import java.io.IOException;

/**
 * Invalid message.
 */
public final class MessageException extends IOException {

    private static final long serialVersionUID = 1L;

    /**
     * Invalid message.
     * @param e root cause
     */
    public MessageException(final IOException e) {
        super(e);
    }
}
