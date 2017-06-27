package nl.futureedge.simple.jmx.it;

import java.io.IOException;
import java.io.Serializable;
import org.springframework.jmx.export.annotation.ManagedAttribute;
import org.springframework.jmx.export.annotation.ManagedOperation;
import org.springframework.jmx.export.annotation.ManagedResource;

@ManagedResource(objectName = "nl.futureedge.simple.jmx.test:name=TEST", description = "JMX Service for test.")
public class JmxObject {

    private int writableAttribuut = 10;

    @ManagedOperation(description = "Method without a return type (void)")
    public void methodNoReturn() {
    }

    @ManagedOperation(description = "Methode with a return type")
    public String methodWithReturn() {
        return "All ok";
    }

    @ManagedAttribute(description = "Readonly attribute")
    public int getReadonlyAttribuut() {
        return 42;
    }

    @ManagedAttribute(description = "Writable attribute")
    public int getWritableAttribuut() {
        return writableAttribuut;
    }

    @ManagedAttribute(description = "Writable attribute")
    public void setWritableAttribuut(final int value) {
        writableAttribuut = value;
    }

    @ManagedAttribute(description = "Big attribute (1k)")
    public String getBigAttribuut1k() {
        return times16("1234567890123456789012345678901234567890123456789012345678901234");
    }

    @ManagedAttribute(description = "Big attribute (16k)")
    public String getBigAttribuut16k() {
        return times16(getBigAttribuut1k());
    }

    @ManagedAttribute(description = "Big attribute (256k)")
    public String getBigAttribuut256k() {
        return times16(getBigAttribuut16k());
    }

    private String times16(final String value) {
        final StringBuilder sb = new StringBuilder();
        for (int i = 0; i < 16; i++) {
            sb.append(value);
        }
        return sb.toString();
    }

    @ManagedAttribute(description = "Attribute to test serialization problems")
    public Object getSerializationProblem() {
        return new Object();
    }

    @ManagedAttribute(description = "Attribute to test serialization problems")
    public Object getSerializationWriteObjectProblem() {
        return new Serializable() {
            private static final long serialVersionUID = 1L;

            private void writeObject(final java.io.ObjectOutputStream out) throws IOException {
                out.writeObject(new Object());
            }

            private void readObject(final java.io.ObjectInputStream in) throws IOException, ClassNotFoundException {
            }
        };
    }

}
