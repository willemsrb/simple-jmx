package nl.futureedge.simple.jmx.utils;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

/**
 * Properties loader that reads from an external file.
 */
public final class PropertiesFileLoader implements PropertiesLoader {

    private final String location;
    private Properties properties;

    /**
     * Create a new properties loader.
     * @param location location of the properties file
     */
    public PropertiesFileLoader(final String location) {
        this.location = location;
    }

    @Override
    public Properties loadProperties() throws IOException {
        if (properties == null) {
            final Properties loadedProperties = new Properties();
            try (InputStream fis = new FileInputStream(location)) {
                loadedProperties.load(fis);
            }
            properties = loadedProperties;
        }
        return properties;
    }
}
