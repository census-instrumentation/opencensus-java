# OpenCensus Zipkin Trace Exporter
[![Build Status][travis-image]][travis-url] [![Build status][appveyor-image]][appveyor-url] [![Maven Central][maven-image]][maven-url]

The *OpenCensus Zipkin Trace Exporter* is a trace exporter that exports
data to Zipkin. [Zipkin](http://zipkin.io/) Zipkin is a distributed
tracing system. It helps gather timing data needed to troubleshoot
latency problems in microservice architectures. It manages both the
collection and lookup of this data.

## Quickstart

### Prerequisites

[Zipkin](http://zipkin.io/) stores and queries traces exported by
applications instrumented with Census. The easiest way to start a zipkin
server is to paste the below:

```bash
wget -O zipkin.jar 'https://search.maven.org/remote_content?g=io.zipkin.java&a=zipkin-server&v=LATEST&c=exec'
java -jar zipkin.jar
```


### Hello Zipkin

#### Add the dependencies to your project

For Maven add to your `pom.xml`:
```xml
<dependencies>
  <dependency>
    <groupId>io.opencensus</groupId>
    <artifactId>opencensus-api</artifactId>
    <version>0.8.0</version>
  </dependency>
  <dependency>
    <groupId>io.opencensus</groupId>
    <artifactId>opencensus-exporter-trace-zipkin</artifactId>
    <version>0.8.0</version>
  </dependency>
  <dependency>
    <groupId>io.opencensus</groupId>
    <artifactId>opencensus-impl</artifactId>
    <version>0.8.0</version>
    <scope>runtime</scope>
  </dependency>
</dependencies>
```

For Gradle add to your dependencies:
```groovy
compile 'io.opencensus:opencensus-api:0.8.0'
compile 'io.opencensus:opencensus-exporter-trace-zipkin:0.8.0'
runtime 'io.opencensus:opencensus-impl:0.8.0'
```

#### Register the exporter

This will report Zipkin v2 json format to a single server. Alternate
[senders](https://github.com/openzipkin/zipkin-reporter-java) are available.

```java
public class MyMainClass {
  public static void main(String[] args) throws Exception {
    ZipkinExporter.createAndRegister("http://127.0.0.1:9411/api/v2/spans");
    // ...
  }
}
```

#### Java Versions

Java 6 or above is required for using this exporter.

[travis-image]: https://travis-ci.org/census-instrumentation/opencensus-java.svg?branch=master
[travis-url]: https://travis-ci.org/census-instrumentation/opencensus-java
[appveyor-image]: https://ci.appveyor.com/api/projects/status/hxthmpkxar4jq4be/branch/master?svg=true
[appveyor-url]: https://ci.appveyor.com/project/instrumentationjavateam/opencensus-java/branch/master
[maven-image]: https://maven-badges.herokuapp.com/maven-central/io.opencensus/opencensus-exporter-trace-zipkin/badge.svg
[maven-url]: https://maven-badges.herokuapp.com/maven-central/io.opencensus/opencensus-exporter-trace-zipkin