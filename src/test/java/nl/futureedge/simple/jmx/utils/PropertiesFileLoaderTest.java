package nl.futureedge.simple.jmx.utils;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Files;
import java.util.Properties;
import org.junit.Assert;
import org.junit.Test;

public class PropertiesFileLoaderTest {

    @Test
    public void test() throws IOException {
        final File directory = Files.createTempDirectory("test").toFile();
        try {
            directory.deleteOnExit();

            final File propertiesFile = new File(directory, "test.properties");
            String propertiesLocation = propertiesFile.getCanonicalPath();

            try {
                new PropertiesFileLoader(propertiesLocation).loadProperties();
                Assert.fail("Should throw IOException");
            } catch (IOException e) {
                // Expected
            }

            final Properties props = new Properties();
            props.put("key", "value");
            props.put("key2", "value2");

            final Properties loaded;
            try {
                try (final OutputStream out = new FileOutputStream(propertiesFile)) {
                    props.store(out, null);
                }
                propertiesFile.deleteOnExit();

                loaded = new PropertiesFileLoader(propertiesLocation).loadProperties();
            } finally {
                propertiesFile.delete();
            }

            Assert.assertEquals(props, loaded);

        } finally {
            directory.delete();
        }

    }

}
