package nl.futureedge.simple.jmx.agent;


import java.io.IOException;
import java.net.ServerSocket;
import java.util.logging.Level;
import java.util.logging.LogManager;
import javax.management.remote.JMXConnector;
import javax.management.remote.JMXConnectorFactory;
import javax.management.remote.JMXServiceURL;
import nl.futureedge.simple.jmx.SimpleJmx;
import org.junit.Assert;
import org.junit.Test;
import org.slf4j.bridge.SLF4JBridgeHandler;

public class AgentTest {

    static {
        // Configure java.util.logging to log via SLF4j
        SLF4JBridgeHandler.removeHandlersForRootLogger();
        SLF4JBridgeHandler.install();
        LogManager.getLogManager().getLogger("").setLevel(Level.FINEST);
    }

    @Test
    public void test() throws IOException {
        final int port = getPort();
        Agent.premain("host=0.0.0.0,port="+Integer.toString(port));

        try (final JMXConnector jmxc = JMXConnectorFactory.connect(new JMXServiceURL(SimpleJmx.PROTOCOL, "0.0.0.0", port), null)) {
            Assert.assertNotNull(jmxc.getConnectionId());
        }
    }

    private int getPort() throws IOException {
        try (final ServerSocket socket = new ServerSocket(0)) {
            return socket.getLocalPort();
        }
    }
}
