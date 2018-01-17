# OpenCensus - A stats collection and distributed tracing framework
[![Gitter chat][gitter-image]][gitter-url]
[![Maven Central][maven-image]][maven-url]
[![Javadocs][javadoc-image]][javadoc-url]
[![Build Status][travis-image]][travis-url]
[![Windows Build Status][appveyor-image]][appveyor-url]
[![Coverage Status][coverage-image]][coverage-url]


OpenCensus is a toolkit for collecting application performance and behavior data. It currently 
includes 3 apis: stats, tracing and tags.

The library is in alpha stage and the API is subject to change.

Please join [gitter](https://gitter.im/census-instrumentation/Lobby) for help or feedback on this
project.

## Instrumentation Quickstart

Integrating OpenCensus with a new library means recording stats or traces and propagating context.

### Add the dependencies to your project

For Maven add to your `pom.xml`:
```xml
<dependencies>
  <dependency>
    <groupId>io.opencensus</groupId>
    <artifactId>opencensus-api</artifactId>
    <version>0.10.1</version>
  </dependency>
</dependencies>
```

For Gradle add to your dependencies:
```gradle
compile 'io.opencensus:opencensus-api:0.10.1'
```

### Hello "OpenCensus" trace events

Here's an example of creating a Span and record some trace annotations. Notice that recording the
annotations is possible because we propagate scope. 3rd parties libraries like SLF4J can integrate
the same way.

```java
public final class MyClassWithTracing {
  public static void doWork() {
    // Create a child Span of the current Span.
    try (Scope ss = tracer.spanBuilder("MyChildWorkSpan").startScopedSpan()) {
      doInitialWork();
      tracer.getCurrentSpan().addAnnotation("Finished initial work");
      doFinalWork();
    }
  }
  
  private static void doInitialWork() {
    // ...
    tracer.getCurrentSpan().addAnnotation("Important.");
    // ...
  }
  
  private static void doFinalWork() {
    // ...
    tracer.getCurrentSpan().addAnnotation("More important.");
    // ...
  }
}
```

### Hello "OpenCensus" stats events

TODO

## Quickstart for Applications

Besides recording tracing/stats events the application also need to link the implementation, 
setup exporters, and debugging [Z-Pages](https://github.com/census-instrumentation/opencensus-java/tree/master/contrib/zpages).

### Add the dependencies to your project

For Maven add to your `pom.xml`:
```xml
<dependencies>
  <dependency>
    <groupId>io.opencensus</groupId>
    <artifactId>opencensus-api</artifactId>
    <version>0.10.1</version>
  </dependency>
  <dependency>
    <groupId>io.opencensus</groupId>
    <artifactId>opencensus-impl</artifactId>
    <version>0.10.1</version>
    <scope>runtime</scope>
  </dependency>
</dependencies>
```

For Gradle add to your dependencies:
```gradle
compile 'io.opencensus:opencensus-api:0.10.1'
runtime 'io.opencensus:opencensus-impl:0.10.1'
```

### How to setup exporters?

#### Trace exporters
* [Logging][TraceExporterLogging]
* [Stackdriver][TraceExporterStackdriver]
* [Zipkin][TraceExporterZipkin]

#### Stats exporters
* [Stackdriver][StatsExporterStackdriver]
* [SignalFx][StatsExporterSignalFx]

### How to setup debugging Z-Pages?

If the application owner wants to export in-process tracing and stats data via HTML debugging pages 
see this [link](https://github.com/census-instrumentation/opencensus-java/tree/master/contrib/zpages#quickstart).

[travis-image]: https://travis-ci.org/census-instrumentation/opencensus-java.svg?branch=master
[travis-url]: https://travis-ci.org/census-instrumentation/opencensus-java
[appveyor-image]: https://ci.appveyor.com/api/projects/status/hxthmpkxar4jq4be/branch/master?svg=true
[appveyor-url]: https://ci.appveyor.com/project/opencensusjavateam/opencensus-java/branch/master
[javadoc-image]: https://www.javadoc.io/badge/io.opencensus/opencensus-api.svg
[javadoc-url]: https://www.javadoc.io/doc/io.opencensus/opencensus-api
[maven-image]: https://maven-badges.herokuapp.com/maven-central/io.opencensus/opencensus-api/badge.svg
[maven-url]: https://maven-badges.herokuapp.com/maven-central/io.opencensus/opencensus-api
[coverage-image]: https://coveralls.io/repos/census-instrumentation/opencensus-java/badge.svg?branch=master&service=github
[coverage-url]: https://coveralls.io/github/census-instrumentation/opencensus-java?branch=master
[gitter-image]: https://badges.gitter.im/census-instrumentation/lobby.svg
[gitter-url]: https://gitter.im/census-instrumentation/lobby?utm_source=badge&utm_medium=badge&utm_campaign=pr-badge&utm_content=badge
[TraceExporterLogging]: https://github.com/census-instrumentation/opencensus-java/tree/master/exporters/trace/logging#quickstart
[TraceExporterStackdriver]: https://github.com/census-instrumentation/opencensus-java/tree/master/exporters/trace/stackdriver#quickstart
[TraceExporterZipkin]: https://github.com/census-instrumentation/opencensus-java/tree/master/exporters/trace/zipkin#quickstart
[StatsExporterStackdriver]: https://github.com/census-instrumentation/opencensus-java/tree/master/exporters/stats/stackdriver#quickstart
[StatsExporterSignalFx]: https://github.com/census-instrumentation/opencensus-java/tree/master/exporters/stats/signalfx#quickstart
