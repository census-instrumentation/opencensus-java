# OpenCensus Http Servlet Plugin
[![Build Status][travis-image]][travis-url]
[![Windows Build Status][appveyor-image]][appveyor-url]
[![Maven Central][maven-image]][maven-url]

The *OpenCensus Http Servlet Plugin for Java* is a plugin for trace instrumentation when using HTTP Servlet 3.0

## Quickstart

### Add the dependencies to your project

For Maven add to your `pom.xml`:
```xml
<dependencies>
  <dependency>
    <groupId>io.opencensus</groupId>
    <artifactId>opencensus-api</artifactId>
    <version>0.19.2</version>
  </dependency>
  <dependency>
    <groupId>io.opencensus</groupId>
    <artifactId>opencensus-contrib-http-servlet</artifactId>
    <version>0.19.2</version>
  </dependency>
</dependencies>
```

For Gradle add to your dependencies:
```groovy
compile 'io.opencensus:opencensus-api:0.19.2'
compile 'io.opencensus:opencensus-contrib-http-servlet:0.19.2'
```

## Instrumenting HTTP Servlets

See [http-server][httpservlet-code] example. Instruction to build and run the example is [here][httpservlet-run].

[travis-image]: https://travis-ci.org/census-instrumentation/opencensus-java.svg?branch=master
[travis-url]: https://travis-ci.org/census-instrumentation/opencensus-java
[appveyor-image]: https://ci.appveyor.com/api/projects/status/hxthmpkxar4jq4be/branch/master?svg=true
[appveyor-url]: https://ci.appveyor.com/project/opencensusjavateam/opencensus-java/branch/master
[maven-image]: https://maven-badges.herokuapp.com/maven-central/io.opencensus/opencensus-contrib-http-servlet/badge.svg
[maven-url]: https://maven-badges.herokuapp.com/maven-central/io.opencensus/opencensus-contrib-http-servlet
[httpservlet-run]: https://github.com/census-instrumentation/opencensus-java/tree/master/examples#to-run-http-server-and-client
[httpservlet-code]: https://github.com/census-instrumentation/opencensus-java/blob/master/examples/src/main/java/io/opencensus/examples/http/jetty/server/HelloWorldServer.java
