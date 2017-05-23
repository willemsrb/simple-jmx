package nl.futureedge.simple.jmx.authenticator;


import org.junit.Assert;
import org.junit.Test;

public class AbstractConfigurationTest {

    @Test
    public void testNoConfiguration() {
        StaticConfiguration configuration = new StaticConfiguration("name");

        Assert.assertNotNull(configuration.getAppConfigurationEntry("name"));
        Assert.assertNull(configuration.getAppConfigurationEntry("othername"));
    }
}
