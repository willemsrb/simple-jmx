package nl.futureedge.simple.jmx.it;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.management.Attribute;
import javax.management.AttributeList;
import javax.management.AttributeNotFoundException;
import javax.management.DynamicMBean;
import javax.management.InvalidAttributeValueException;
import javax.management.MBeanException;
import javax.management.MBeanInfo;
import javax.management.Notification;
import javax.management.NotificationListener;
import javax.management.ReflectionException;

import org.springframework.jmx.export.annotation.ManagedResource;
 
@ManagedResource(objectName = "nl.futureedge.simple.jmx.test:name=LISTENER", description = "JMX Notification Listener for test.")
public class JmxListener implements NotificationListener, DynamicMBean {

    private static final Logger LOGGER = Logger.getLogger(JmxListener.class.getName());

    private final List<Notification> notifications = new ArrayList<>();

    @Override
    public void handleNotification(final Notification notification, final Object handback) {
        LOGGER.log(Level.INFO, "Received notification: {0}", notification);
        notifications.add(notification);
    }

    public List<Notification> getNotifications() {
        return notifications;
    }

    @Override
    public Object getAttribute(final String attribute) throws AttributeNotFoundException, MBeanException, ReflectionException {
        return null;
    }

    @Override
    public void setAttribute(final Attribute attribute)
        throws AttributeNotFoundException, InvalidAttributeValueException, MBeanException, ReflectionException
    {
    }

    @Override
    public AttributeList getAttributes(final String[] attributes) {
        return null;
    }

    @Override
    public AttributeList setAttributes(final AttributeList attributes) {
        return null;
    }

    @Override
    public Object invoke(final String actionName, final Object[] params, final String[] signature) throws MBeanException, ReflectionException {
        return null;
    }

    @Override
    public MBeanInfo getMBeanInfo() {
        return new MBeanInfo(JmxListener.class.getName(), "JMX Notification Listener for test.", null, null, null, null);
    }

}
