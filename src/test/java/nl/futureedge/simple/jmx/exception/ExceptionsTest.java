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
}
