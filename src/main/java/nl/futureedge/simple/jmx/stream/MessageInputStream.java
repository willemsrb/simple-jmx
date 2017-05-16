package nl.futureedge.simple.jmx.stream;

import java.io.EOFException;
import java.io.IOException;
import java.io.InputStream;
import java.util.logging.Level;
import java.util.logging.Logger;
import nl.futureedge.simple.jmx.message.Message;

/**
 * Stream to receive {@link Message Messages}.
 *
 * <p>
 * Note: the wrapped InputStream is not closed directly. It is
 * {@link java.net.Socket#close() closed} by the socket.
 * </p>
 */
public final class MessageInputStream {

    private static final Logger LOGGER = Logger.getLogger(MessageInputStream.class.getName());

    private final InputStream input;

    /**
     * Constructor.
     * @param base base input stream
     * @throws IOException on I/O errors
     */
    public MessageInputStream(final InputStream base) throws IOException {
        input = base;
    }

    /**
     * Read a message.
     * @return a message
     * @throws IOException on I/O errros
     */
    public Message read() throws IOException {
        synchronized (input) {
            LOGGER.log(Level.FINE, "Reading length (waiting for message) ...");
            final int length = StreamUtils.deserializeLength(readData(4));
            LOGGER.log(Level.FINE, "Reading data (length {0,number,######}) ...", length);
            final Message message = StreamUtils.deserializeMessage(readData(length));
            LOGGER.log(Level.FINE, "Message received");
            return message;
        }
    }

    private byte[] readData(final int length) throws IOException {
        final byte[] data = new byte[length];
        int totalRead = 0;
        while (totalRead < length) {
            final int read = input.read(data, totalRead, length - totalRead);
            if (read == -1) {
                throw new EOFException();
            }
            totalRead = +read;
        }
        return data;
    }

}
