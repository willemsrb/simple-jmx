package nl.futureedge.simple.jmx.utils;

import java.io.Closeable;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * IO utility methods.
 */
public final class IOUtils {

    private static final Logger LOGGER = Logger.getLogger(IOUtils.class.getName());

    private IOUtils() {
        throw new IllegalStateException("Do not instantiate");
    }

    /**
     * Close, ignoring IO exceptions.
     * @param closeable object to close
     */
    public static void closeSilently(final Closeable closeable) {
        ignoreIOException(closeable::close);
    }

    /**
     * Execute an IO function ignoring the IOException.
     * @param ioFunction IO function
     */
    public static void ignoreIOException(final IOFunction ioFunction) {
        try {
            ioFunction.execute();
        } catch (IOException e) {
            // Ignore
            LOGGER.log(Level.FINE, "Ignoring exception", e);
        }
    }

    /**
     * IO Function.
     */
    @FunctionalInterface
    public interface IOFunction {

        /**
         * Execute function.
         * @throws IOException if an I/O error occurs when executing this function
         */
        void execute() throws IOException;
    }
}
