package nl.futureedge.simple.jmx.message;

import java.util.UUID;

/**
 * Base request.
 */
public class Request implements Message {
    private static final long serialVersionUID = 1L;

    private final String requestId = UUID.randomUUID().toString();

    Request() {
    }

    /**
     * Het unieke request id waarop de response gecorreleerd kan worden.
     * @return unieke request id.
     */
    public final String getRequestId() {
        return requestId;
    }

}
