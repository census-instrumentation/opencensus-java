# OpenCensus Jaeger Trace Exporter
[![Build Status][travis-image]][travis-url]
[![Windows Build Status][appveyor-image]][appveyor-url]
[![Maven Central][maven-image]][maven-url]

The *OpenCensus Jaeger Trace Exporter* is a trace exporter that exports
data to Jaeger.

[Jaeger](https://jaeger.readthedocs.io/en/latest/), inspired by [Dapper](https://research.google.com/pubs/pub36356.html) and [OpenZipkin](http://zipkin.io/), is a distributed tracing system released as open source by [Uber Technologies](http://uber.github.io/). It is used for monitoring and troubleshooting microservices-based distributed systems, including:

- Distributed context propagation
- Distributed transaction monitoring
- Root cause analysis
- Service dependency analysis
- Performance / latency optimization

## Quickstart

### Prerequisites

[Jaeger](https://jaeger.readthedocs.io/en/latest/) stores and queries traces exported by
applications instrumented with Census. The easiest way to [start a Jaeger
server](https://jaeger.readthedocs.io/en/latest/getting_started/) is to paste the below:

```bash
docker run -d \
    -e COLLECTOR_ZIPKIN_HTTP_PORT=9411 \
    -p5775:5775/udp -p6831:6831/udp -p6832:6832/udp \
    -p5778:5778 -p16686:16686 -p14268:14268 -p9411:9411 \
  jaegertracing/all-in-one:latest
```

### Hello Jaeger

#### Add the dependencies to your project

For Maven add to your `pom.xml`:
```xml
<dependencies>
  <dependency>
    <groupId>io.opencensus</groupId>
    <artifactId>opencensus-api</artifactId>
    <version>0.17.0</version>
  </dependency>
  <dependency>
    <groupId>io.opencensus</groupId>
    <artifactId>opencensus-exporter-trace-jaeger</artifactId>
    <version>0.17.0</version>
  </dependency>
  <dependency>
    <groupId>io.opencensus</groupId>
    <artifactId>opencensus-impl</artifactId>
    <version>0.17.0</version>
    <scope>runtime</scope>
  </dependency>
</dependencies>
```

For Gradle add to your dependencies:
```groovy
compile 'io.opencensus:opencensus-api:0.17.0'
compile 'io.opencensus:opencensus-exporter-trace-jaeger:0.17.0'
runtime 'io.opencensus:opencensus-impl:0.17.0'
```

#### Register the exporter

This will export traces to the Jaeger thrift format to the Jaeger instance started previously:

```java
public class MyMainClass {
  public static void main(String[] args) throws Exception {
    JaegerTraceExporter.createAndRegister("http://127.0.0.1:14268/api/traces", "my-service");
    // ...
  }
}
```

See also [this integration test](https://github.com/census-instrumentation/opencensus-java/blob/master/exporters/trace/jaeger/src/test/java/io/opencensus/exporter/trace/jaeger/JaegerExporterHandlerIntegrationTest.java).

#### Java Versions

Java 6 or above is required for using this exporter.

[travis-image]: https://travis-ci.org/census-instrumentation/opencensus-java.svg?branch=master
[travis-url]: https://travis-ci.org/census-instrumentation/opencensus-java
[appveyor-image]: https://ci.appveyor.com/api/projects/status/hxthmpkxar4jq4be/branch/master?svg=true
[appveyor-url]: https://ci.appveyor.com/project/opencensusjavateam/opencensus-java/branch/master
[maven-image]: https://maven-badges.herokuapp.com/maven-central/io.opencensus/opencensus-exporter-trace-jaeger/badge.svg
[maven-url]: https://maven-badges.herokuapp.com/maven-central/io.opencensus/opencensus-exporter-trace-jaeger
