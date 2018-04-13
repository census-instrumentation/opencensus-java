# OpenCensus Instana Trace Exporter
[![Build Status][travis-image]][travis-url]
[![Windows Build Status][appveyor-image]][appveyor-url]
[![Maven Central][maven-image]][maven-url]

The *OpenCensus Instana Trace Exporter* is a trace exporter that exports
data to Instana. [Instana](http://www.instana.com/) is a distributed
tracing system.

## Quickstart

### Prerequisites

[Instana](http://www.instana.com/) forwards traces exported by applications
instrumented with Census to its backend using the Instana agent processes as proxy.
If the agent is used on the same host as Census, please take care to deactivate
automatic tracing.


### Hello Stan

#### Add the dependencies to your project

For Maven add to your `pom.xml`:
```xml
<dependencies>
  <dependency>
    <groupId>io.opencensus</groupId>
    <artifactId>opencensus-api</artifactId>
    <version>0.12.3</version>
  </dependency>
  <dependency>
    <groupId>io.opencensus</groupId>
    <artifactId>opencensus-exporter-trace-instana</artifactId>
    <version>0.12.3</version>
  </dependency>
  <dependency>
    <groupId>io.opencensus</groupId>
    <artifactId>opencensus-impl</artifactId>
    <version>0.12.3</version>
    <scope>runtime</scope>
  </dependency>
</dependencies>
```

For Gradle add to your dependencies:
```groovy
compile 'io.opencensus:opencensus-api:0.12.3'
compile 'io.opencensus:opencensus-exporter-trace-instana:0.12.3'
runtime 'io.opencensus:opencensus-impl:0.12.3'
```

#### Register the exporter

```java
public class MyMainClass {
  public static void main(String[] args) throws Exception {
    InstanaTraceExporter.createAndRegister("http://localhost:42699/com.instana.plugin.generic.trace");
    // ...
  }
}
```

#### Java Versions

Java 6 or above is required for using this exporter.

[travis-image]: https://travis-ci.org/census-instrumentation/opencensus-java.svg?branch=master
[travis-url]: https://travis-ci.org/census-instrumentation/opencensus-java
[appveyor-image]: https://ci.appveyor.com/api/projects/status/hxthmpkxar4jq4be/branch/master?svg=true
[appveyor-url]: https://ci.appveyor.com/project/opencensusjavateam/opencensus-java/branch/master
[maven-image]: https://maven-badges.herokuapp.com/maven-central/io.opencensus/opencensus-exporter-trace-instana/badge.svg
[maven-url]: https://maven-badges.herokuapp.com/maven-central/io.opencensus/opencensus-exporter-trace-instana
