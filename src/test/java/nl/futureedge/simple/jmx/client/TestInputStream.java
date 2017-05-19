package nl.futureedge.simple.jmx.client;


import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InterruptedIOException;
import java.util.ArrayList;
import java.util.List;
import nl.futureedge.simple.jmx.message.Message;
import nl.futureedge.simple.jmx.stream.MessageOutputStream;

public class TestInputStream extends InputStream {

    private final Object readLock = new Object();
    private final List<byte[]> registeredData = new ArrayList<>();
    private byte[] currentData = new byte[]{};
    private int currentDataIndex = 0;
    private volatile boolean done = false;

    public void registerMessage(Message message) {
        ByteArrayOutputStream buffer = new ByteArrayOutputStream();
        try {
            MessageOutputStream output = new MessageOutputStream(buffer);
            output.write(message);
        } catch(IOException e) {
            throw new IllegalArgumentException("Could not write message", e);
        }
        registerData(buffer.toByteArray());
    }

    public void registerData(byte[] data) {
        synchronized (readLock) {
            if (done) {
                throw new IllegalStateException("TestInputStream cannot accepted more data as done() was called");
            }
            if (data == null || data.length == 0) {
                return;
            }
            registeredData.add(data);
            readLock.notifyAll();
        }
    }

    public void done() {
        synchronized (readLock) {
            done = true;
            readLock.notifyAll();
        }
    }

    @Override
    public int read() throws IOException {
        synchronized (readLock) {
            while (currentDataIndex >= currentData.length) {
                if (done) {
                    return -1;
                }

                if (registeredData.isEmpty()) {
                    try {
                        readLock.wait();
                    } catch(InterruptedException e) {
                        throw new InterruptedIOException();
                    }
                } else {
                    // Next data
                    currentData = registeredData.remove(0);
                    currentDataIndex = 0;
                }
            }

            return currentData[currentDataIndex++];
        }
    }
}
