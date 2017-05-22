package nl.futureedge.simple.jmx.utils;

import java.io.IOException;
import java.util.Properties;

/**
 * Properties loader.
 */
@FunctionalInterface
public interface PropertiesLoader {

    /**
     * Load the properties.
     * @return properties
     * @throws IOException if an I/O error occurs when loading the properties
     */
    public Properties loadProperties() throws IOException;
}
