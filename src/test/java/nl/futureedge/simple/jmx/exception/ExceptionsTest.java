package nl.futureedge.simple.jmx.exception;

import org.junit.Test;

public class ExceptionsTest {

    @Test(expected = InvalidCredentialsException.class)
    public void invalidCredentials() throws InvalidCredentialsException {
        throw new InvalidCredentialsException();
    }

    @Test(expected = NotLoggedOnException.class)
    public void notLoggedOn() throws NotLoggedOnException {
        throw new NotLoggedOnException();
    }

    @Test(expected = UnknownRequestException.class)
    public void unknownRequest() throws UnknownRequestException {
        throw new UnknownRequestException();
    }

    @Test(expected = RequestTimedOutException.class)
    public void requestTimedOut() throws RequestTimedOutException {
        throw new RequestTimedOutException();
    }
}
