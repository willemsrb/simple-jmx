## Spring example
Spring can be used to start and configure any JMX (server) connector.
The following example shows how to configure the JMX server connector.

```
<?xml version="1.0" encoding="UTF-8"?>
<beans xmlns="http://www.springframework.org/schema/beans"
       xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
       xsi:schemaLocation="http://www.springframework.org/schema/beans http://www.springframework.org/schema/beans/spring-beans.xsd">

    <bean id="testMBeanServer" class="org.springframework.jmx.support.MBeanServerFactoryBean">
        <!-- Use only PlatformMBeanServer -->
        <property name="agentId" value=""/>
    </bean>

    <bean id="testServerConnector" class="org.springframework.jmx.support.connectorserverfactorybean">
        <property name="serviceUrl" value="service:jmx:simple://0.0.0.0:3481"/>
        <property name="server" ref="testMBeanServer" />
        <property name="environmentMap">
            <map>
                <entry key="jmx.remote.authenticator">
                    <bean class="nl.futureedge.simple.jmx.authenticator.PropertiesAuthenticator">
                        <constructor-arg>
                            <props>
                                <prop key="admin">admin</prop>
                                <prop key="reader">reader</prop>
                            </props>
                        </constructor-arg>
                    </bean>
                </entry>
                <entry key="jmx.remote.accesscontroller">
                    <bean class="nl.futureedge.simple.jmx.access.PropertiesAccessController">
                        <constructor-arg>
                            <props>
                                <prop key="admin">readwrite</prop>
                                <prop key="reader">readonly</prop>
                            </props>
                        </constructor-arg>
                    </bean>
                </entry>
            </map>
        </property>
    </bean>
</beans>
```
The next example shows how to configure the client JMX connector:
```
<?xml version="1.0" encoding="UTF-8"?>
<beans xmlns="http://www.springframework.org/schema/beans"
       xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
       xsi:schemaLocation="http://www.springframework.org/schema/beans http://www.springframework.org/schema/beans/spring-beans.xsd">

    <bean id="testClientConnector" class="org.springframework.jmx.support.MBeanServerConnectionFactoryBean">
        <property name="serviceUrl" value="service:jmx:simple://localhost:3481"/>
    	<property name="environment">
			<map>
				<entry key="jmx.remote.credentials">
                	<array>
                    	<value>admin</value>
                        <value>admin</value>
                    </array>
                </entry>
			</map>
    	</property>
    </bean>
</beans>
```