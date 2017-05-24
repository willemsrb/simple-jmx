package nl.futureedge.simple.jmx.socket;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import javax.management.remote.JMXServiceURL;

/**
 * Socket factory.
 */
public interface JMXSocketFactory {

    /**
     * Create a new client socket.
     * @param serviceUrl jmx service url
     * @return client socket
     * @throws IOException if an I/O error occurs during the creation of the socket
     */
    Socket createSocket(JMXServiceURL serviceUrl) throws IOException;

    /**
     * Create a new server socket.
     * @param serviceUrl jmx service url
     * @return server socket
     * @throws IOException if an I/O error occurs during the creation of the socket
     */
    ServerSocket createServerSocket(JMXServiceURL serviceUrl) throws IOException;

}
