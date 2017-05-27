## Java Agent example
Simple JMX can be started as a Java Agent

`java -javaagent:simple-jmx.jar[=options]`

The following options can be used

| option        | description |
|---------------|--------|
| host          | interface to bind to (default 0.0.0.0)
| port          | port to bind to (default 3481)
| login.config  | configures an external authenticator using the given name
| password.file | configures a properties authenticator using the given property file location
| access.file   | configures a properties access controller using the given property file location
