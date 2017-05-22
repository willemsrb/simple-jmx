package nl.futureedge.simple.jmx.message;

import java.util.concurrent.atomic.AtomicLong;

/**
 * Base request.
 */
public class Request implements Message {

    private static final long serialVersionUID = 1L;
    private static final AtomicLong REQUEST_ID = new AtomicLong(1);

    private final String requestId = Long.toHexString(REQUEST_ID.getAndIncrement());

    /**
     * Create a new request.
     */
    Request() {
    }

    /**
     * The unique request id a response can be correlated on.
     * @return unique request id.
     */
    public final String getRequestId() {
        return requestId;
    }

}
