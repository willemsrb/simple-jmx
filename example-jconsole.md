## JConsole example
You can use Simple JMX with JConsole by adding the JAR file to the classpath:

`$JAVA_HOME\bin\jconsole -J-Djava.class.path=$JAVA_HOME\lib\jconsole.jar;$PATH_TO\simple-jmx.jar`

Where $JAVA_HOME references the installation directory of Java and $PATH_TO references a directory where the JAR file of Simple JMX (simple-jmx.jar) can be found