package nl.futureedge.simple.jmx.stream;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.EOFException;
import java.io.IOException;
import nl.futureedge.simple.jmx.message.RequestLogoff;
import nl.futureedge.simple.jmx.message.RequestLogon;
import org.junit.Assert;
import org.junit.Test;

public class MessageStreamTest {

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
}
