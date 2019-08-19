package nl.futureedge.simple.jmx.socket;

import javax.management.remote.JMXServiceURL;
import java.io.IOException;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Socket factory without any security at all.
 */
public class PlainSocketFactory implements JMXSocketFactory {

	private static final int BACKLOG = 50;
	private static final Logger LOGGER = Logger.getLogger(PlainSocketFactory.class.getName());

	/**
	 * Create a client socket.
	 * @param serviceUrl jmx service url
	 * @return client socket
	 * @throws IOException if an I/O error occurs when creating the socket
	 */
	@Override
	public Socket createSocket(final JMXServiceURL serviceUrl) throws IOException {
		final Socket socket = new Socket(serviceUrl.getHost(), serviceUrl.getPort());
		socket.setKeepAlive(true);

		LOGGER.log(Level.FINE, "Created client socket");
		return socket;
	}

	/**
	 * Create a server socket.
	 * @param serviceUrl jmx service url
	 * @return server socket
	 * @throws IOException if an I/O error occurs when creating the socket
	 */
	@Override
	public ServerSocket createServerSocket(final JMXServiceURL serviceUrl) throws IOException {
		final InetAddress host = InetAddress.getByName(serviceUrl.getHost());
		final ServerSocket serverSocket = new ServerSocket(serviceUrl.getPort(), BACKLOG, host);

		LOGGER.log(Level.FINE, "Created server socket");
		return serverSocket;
	}
}
