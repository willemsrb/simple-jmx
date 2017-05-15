# Simple JMX [![Build Status](https://travis-ci.org/willemsrb/simple-jmx.svg?branch=master)](https://travis-ci.org/willemsrb/simple-jmx) [![Quality Gate](https://sonarqube.com/api/badges/gate?key=nl.future-edge.simple:simple-jmx)](https://sonarqube.com/dashboard/index?id=nl.future-edge.simple%3Asimple-jmx)
*Simple JMX protocol that works without RMI.*

Have you ever created a java application running in a Docker container and wanted to connect using JMX? Have you ever struggled configuring JMX to work through a firewall? Then you have probably cursed the RMI protocol for sending a server and a data port to the client, slamming into your firewall or completely ignoring your Docker port mapping.

Simple JMX solves this problem by using a simple protocol to support JMX. Simple JMX does not require any data channel or callback to work and only opens a connection directly to the server port.


