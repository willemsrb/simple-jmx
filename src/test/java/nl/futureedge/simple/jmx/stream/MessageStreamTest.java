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
        ByteArrayOutputStream buffer = new ByteArrayOutputStream();
        MessageOutputStream output = new MessageOutputStream(buffer);
        output.write(new RequestLogon("user", "pass"));
        output.write(new RequestLogoff());
        buffer.close();

        MessageInputStream input = new MessageInputStream(new ByteArrayInputStream(buffer.toByteArray()));
        RequestLogon logon = (RequestLogon)input.read();
        RequestLogoff logoff = (RequestLogoff)input.read();
        try {
            input.read();
            Assert.fail("Exception expected");
        } catch(EOFException e) {
            // Expected
        }
    }
}
