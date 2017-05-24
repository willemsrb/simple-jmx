package nl.futureedge.simple.jmx.agent;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;
import java.util.Map;
import nl.futureedge.simple.jmx.utils.IOUtils;
import org.junit.Assert;
import org.junit.Test;

public class ArgumentSplitterTest {

    @Test
    public void test() {
        Map<String,String> expected = new HashMap<>();
        Assert.assertEquals(expected, ArgumentSplitter.split(null));
        Assert.assertEquals(expected, ArgumentSplitter.split(""));
        Assert.assertEquals(expected, ArgumentSplitter.split("    "));

        expected.put("host", "192.168.178.23");
        Assert.assertEquals(expected, ArgumentSplitter.split("host=192.168.178.23"));

        expected.put("port", "0");
        Assert.assertEquals(expected, ArgumentSplitter.split("host=192.168.178.23,port=0"));
        Assert.assertEquals(expected, ArgumentSplitter.split("   host   =   192.168.178.23   ,   port   =   0   "));
        Assert.assertEquals(expected, ArgumentSplitter.split("port=0,host=192.168.178.23"));

        expected.put("login.config", "MyLoginConfiguration");
        Assert.assertEquals(expected, ArgumentSplitter.split("login.config=MyLoginConfiguration,host=192.168.178.23,port=0"));
        Assert.assertEquals(expected, ArgumentSplitter.split("    host    =    192.168.178.23    ,    login.config   =   MyLoginConfiguration   ,   port   =   0   "));
        Assert.assertEquals(expected, ArgumentSplitter.split("host=192.168.178.23,port=0,login.config=MyLoginConfiguration"));

        expected.put("access.file", "");
        Assert.assertEquals(expected, ArgumentSplitter.split("access.file=,host=192.168.178.23,port=0,login.config=MyLoginConfiguration"));
        Assert.assertEquals(expected, ArgumentSplitter.split("  host  =  192.168.178.23  ,  port  =  0  ,  login.config  =  MyLoginConfiguration  ,  access.file  =  "));
        Assert.assertEquals(expected, ArgumentSplitter.split("host=192.168.178.23,access.file=,port=0,login.config=MyLoginConfiguration,access.file="));

        Assert.assertEquals(expected, ArgumentSplitter.split("access.file,host=192.168.178.23,port=0,login.config=MyLoginConfiguration"));
        Assert.assertEquals(expected, ArgumentSplitter.split("  host  =  192.168.178.23  ,  port  =  0  ,  access.file  ,  login.config  =  MyLoginConfiguration  "));
        Assert.assertEquals(expected, ArgumentSplitter.split("host=192.168.178.23,port=0,login.config=MyLoginConfiguration,access.file"));
    }


    @Test
    public void restrictedConstructor() throws ReflectiveOperationException {
        final Constructor<?> constructor = ArgumentSplitter.class.getDeclaredConstructor();
        constructor.setAccessible(true);
        try {
            constructor.newInstance();
            Assert.fail("Constructor should fail");
        } catch (final InvocationTargetException e) {
            Assert.assertEquals(IllegalStateException.class, e.getCause().getClass());
        }
    }
}
