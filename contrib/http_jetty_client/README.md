# OpenCensus Jetty HttpClient
[![Build Status][travis-image]][travis-url]
[![Windows Build Status][appveyor-image]][appveyor-url]
[![Maven Central][maven-image]][maven-url]

The *OpenCensus Jetty HttpClient for Java* is a wrapper for trace instrumentation when using Jetty as HTTP client.

## Quickstart

### Add the dependencies to your project

For Maven add to your `pom.xml`:
```xml
<dependencies>
  <dependency>
    <groupId>io.opencensus</groupId>
    <artifactId>opencensus-api</artifactId>
    <version>0.19.0</version>
  </dependency>
  <dependency>
    <groupId>io.opencensus</groupId>
    <artifactId>opencensus-contrib-http-jetty-client</artifactId>
    <version>0.19.0</version>
  </dependency>
</dependencies>
```

For Gradle add to your dependencies:
```groovy
compile 'io.opencensus:opencensus-api:0.19.0'
compile 'io.opencensus:opencensus-contrib-http-jetty-client:0.19.0'
```

## Instrumenting Jetty Http Client

See [http-client][httpclient-code] example. For build and run instruction click [here][httpclient-run].


[travis-image]: https://travis-ci.org/census-instrumentation/opencensus-java.svg?branch=master
[travis-url]: https://travis-ci.org/census-instrumentation/opencensus-java
[appveyor-image]: https://ci.appveyor.com/api/projects/status/hxthmpkxar4jq4be/branch/master?svg=true
[appveyor-url]: https://ci.appveyor.com/project/opencensusjavateam/opencensus-java/branch/master
[maven-image]: https://maven-badges.herokuapp.com/maven-central/io.opencensus/opencensus-contrib-http-jetty-client/badge.svg
[maven-url]: https://maven-badges.herokuapp.com/maven-central/io.opencensus/opencensus-contrib-jetty-client
[httpclient-code]: https://github.com/census-instrumentation/opencensus-java/blob/master/examples/src/main/java/io/opencensus/examples/http/jetty/client/HelloWorldClient.java
[httpclient-run]: https://github.com/census-instrumentation/opencensus-java/blob/master/examples/README.md#to-run-http-server-and-client
