package nl.futureedge.simple.jmx.message;

/**
 * Request sent before disconnecting; requests the server to shutdown all notifications.
 */
public final class RequestLogoff extends Request {

	private static final long serialVersionUID = 1L;

	@Override
	public String toString() {
		return "RequestLogoff [requestId=" + getRequestId() + "]";
	}

}
