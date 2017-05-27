## Java example
The following code shows how to start a JMX Server Connector:
```
// Connection
final String host = "0.0.0.0";
final int port = 3481;
final JMXServiceURL url = new JMXServiceURL(SimpleJmx.PROTOCOL, host, port);

// Environment
final Map<String, Object> environment = new HashMap<>();
environment.put(Environment.KEY_AUTHENTICATOR, new StaticAuthenticator());
environment.put(Environment.KEY_ACCESSCONTROLLER, new AllAccessController());

// MBean server
final MBeanServer mbeanServer = ManagementFactory.getPlatformMBeanServer();

// Server
final JMXConnectorServer server = new ServerProvider().newJMXConnectorServer(url, environment, mbeanServer);
try {
	server.start();

	// Wait until server should be shut down

} finally {
	server.stop();
}

```

The following code show how to start a JMX (Client) connector:
```
// Connection
final String host = "0.0.0.0";
final int port = 3481;
final JMXServiceURL url = new JMXServiceURL(SimpleJmx.PROTOCOL, host, port);

// Credentials
final Map<String, Object> environment = new HashMap<>();
environment.put(JMXConnector.CREDENTIALS, new String[]{"reader", "reader"});

// Client
try (final JMXConnector jmxc = JMXConnectorFactory.connect(url, environment)) {
	final MBeanServerConnection serverConnection = jmxc.getMBeanServerConnection();

	// Do stuff using the mbean server connection
}

```