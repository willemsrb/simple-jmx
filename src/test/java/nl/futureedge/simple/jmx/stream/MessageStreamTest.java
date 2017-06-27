package nl.futureedge.simple.jmx.stream;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.EOFException;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.LogManager;
import nl.futureedge.simple.jmx.message.RequestLogoff;
import nl.futureedge.simple.jmx.message.RequestLogon;
import org.junit.Assert;
import org.junit.Test;
import org.slf4j.bridge.SLF4JBridgeHandler;

public class MessageStreamTest {

    static {
        // Configure java.util.logging to log via SLF4j
        SLF4JBridgeHandler.removeHandlersForRootLogger();
        SLF4JBridgeHandler.install();
        LogManager.getLogManager().getLogger("").setLevel(Level.FINEST);
    }

    @Test
    public void test() throws IOException {
        final ByteArrayOutputStream buffer = new ByteArrayOutputStream();
        final MessageOutputStream output = new MessageOutputStream(buffer);
        output.write(new RequestLogon(new String[]{"user", "pass"}));
        output.write(new RequestLogoff());
        buffer.close();

        final MessageInputStream input = new MessageInputStream(new ByteArrayInputStream(buffer.toByteArray()));
        final RequestLogon logon = (RequestLogon) input.read();
        Assert.assertNotNull(logon);
        final RequestLogoff logoff = (RequestLogoff) input.read();
        Assert.assertNotNull(logoff);
        try {
            input.read();
            Assert.fail("Exception expected");
        } catch (final EOFException e) {
            // Expected
        }
    }

    @Test
    public void testBigString() throws IOException {
        String data = times16(times16("1234567890123456789012345678901234567890123456789012345678901234"));

        final ByteArrayOutputStream buffer = new ByteArrayOutputStream();
        final MessageOutputStream output = new MessageOutputStream(buffer);
        output.write(new RequestLogon(new String[]{"user", data}));
        output.write(new RequestLogoff());
        buffer.close();

        final MessageInputStream input = new MessageInputStream(new ByteArrayInputStream(buffer.toByteArray()));
        final RequestLogon logon = (RequestLogon) input.read();
        Assert.assertNotNull(logon);
        Assert.assertEquals(data, ((String[]) logon.getCredentials())[1]);
        final RequestLogoff logoff = (RequestLogoff) input.read();
        Assert.assertNotNull(logoff);
        try {
            input.read();
            Assert.fail("Exception expected");
        } catch (final EOFException e) {
            // Expected
        }
    }

    private String times16(final String value) {
        final StringBuilder sb = new StringBuilder();
        for (int i = 0; i < 16; i++) {
            sb.append(value);
        }
        return sb.toString();
    }
}
