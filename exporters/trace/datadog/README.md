# OpenCensus Datadog Trace Exporter
[![Build Status][travis-image]][travis-url]
[![Windows Build Status][appveyor-image]][appveyor-url]
[![Maven Central][maven-image]][maven-url]

The *OpenCensus Datadog Trace Exporter* is a trace exporter that exports data to [Datadog](https://www.datadoghq.com/).

## Quickstart

### Prerequisites

Datadog collects traces using a local agent, which forwards them to the Datadog automatic tracing. Instructions for setting up the agent can be found [in the Datadog docs](https://docs.datadoghq.com/agent/?tab=agentv6).

### Hello Datadog

#### Add the dependencies to your project

For Maven add to your `pom.xml`:
```xml
<dependencies>
  <dependency>
    <groupId>io.opencensus</groupId>
    <artifactId>opencensus-api</artifactId>
    <version>0.31.0</version>
  </dependency>
  <dependency>
    <groupId>io.opencensus</groupId>
    <artifactId>opencensus-exporter-trace-datadog</artifactId>
    <version>0.31.0</version>
  </dependency>
  <dependency>
    <groupId>io.opencensus</groupId>
    <artifactId>opencensus-impl</artifactId>
    <version>0.31.0</version>
    <scope>runtime</scope>
  </dependency>
</dependencies>
```

For Gradle add to your dependencies:
```groovy
compile 'io.opencensus:opencensus-api:0.31.0'
compile 'io.opencensus:opencensus-exporter-trace-datadog:0.31.0'
runtime 'io.opencensus:opencensus-impl:0.31.0'
```

#### Register the exporter

```java
public class MyMainClass {
  public static void main(String[] args) throws Exception {

    DatadogTraceConfiguration config = DatadogTraceConfiguration.builder()
      .setAgentEndpoint("http://localhost:8126/v0.3/traces")
      .setService("myService")
      .setType("web")
      .build();
    DatadogTraceExporter.createAndRegister(config);
    // ...
  }
}
```

#### Java Versions

Java 8 or above is required for using this exporter.

[travis-image]: https://travis-ci.org/census-instrumentation/opencensus-java.svg?branch=master
[travis-url]: https://travis-ci.org/census-instrumentation/opencensus-java
[appveyor-image]: https://ci.appveyor.com/api/projects/status/hxthmpkxar4jq4be/branch/master?svg=true
[appveyor-url]: https://ci.appveyor.com/project/opencensusjavateam/opencensus-java/branch/master
[maven-image]: https://maven-badges.herokuapp.com/maven-central/io.opencensus/opencensus-exporter-trace-datadog/badge.svg
[maven-url]: https://maven-badges.herokuapp.com/maven-central/io.opencensus/opencensus-exporter-trace-datadog
