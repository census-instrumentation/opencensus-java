# OpenCensus - A stats collection and distributed tracing framework
[![Build Status][travis-image]][travis-url] [![Build status][appveyor-image]][appveyor-url] [![Maven Central][maven-image]][maven-url]

OpenCensus provides a framework to define and collect stats against metrics and to break those 
stats down across user-defined dimensions. The library is in alpha stage and the API is subject 
to change.

## Quickstart for Third Party Libraries

In general third party libraries only need to record tracing/stats events and propagate the context.

### Add the dependencies to your project

For Maven add to your `pom.xml`:
```xml
<dependencies>
  <dependency>
    <groupId>io.opencensus</groupId>
    <artifactId>opencensus-api</artifactId>
    <version>0.6.0</version>
  </dependency>
</dependencies>
```

For Gradle add to your dependencies:
```gradle
compile 'io.opencensus:opencensus-api:0.6.0'
```

### Hello "OpenCensus" trace events

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
setup exporters, and debugging Z-Pages.

### Add the dependencies to your project

For Maven add to your `pom.xml`:
```xml
<dependencies>
  <dependency>
    <groupId>io.opencensus</groupId>
    <artifactId>opencensus-api</artifactId>
    <version>0.6.0</version>
  </dependency>
  <dependency>
    <groupId>io.opencensus</groupId>
    <artifactId>opencensus-impl</artifactId>
    <version>0.6.0</version>
    <scope>runtime</scope>
  </dependency>
</dependencies>
```

For Gradle add to your dependencies:
```gradle
compile 'io.opencensus:opencensus-api:0.6.0'
runtime 'io.opencensus:opencensus-impl:0.6.0'
```

### How to setup exporters?

#### Trace exporters
* [Logging](https://github.com/census-instrumentation/opencensus-java/tree/master/exporters/trace_logging#quickstart)
* [Stackdriver Trace](https://github.com/census-instrumentation/opencensus-java/tree/master/exporters/trace_stackdriver#quickstart)

#### Stats exporters
* TODO

### How to setup debugging Z-Pages?

If the application owner wants to export in-process tracing and stats data via HTML debugging pages 
see this [link](https://github.com/census-instrumentation/opencensus-java/tree/master/contrib/zpages#quickstart).

[travis-image]: https://travis-ci.org/census-instrumentation/opencensus-java.svg?branch=master
[travis-url]: https://travis-ci.org/census-instrumentation/opencensus-java
[appveyor-image]: https://ci.appveyor.com/api/projects/status/hxthmpkxar4jq4be/branch/master?svg=true
[appveyor-url]: https://ci.appveyor.com/project/instrumentationjavateam/opencensus-java/branch/master
[maven-image]: https://maven-badges.herokuapp.com/maven-central/io.opencensus/opencensus-api/badge.svg
[maven-url]: https://maven-badges.herokuapp.com/maven-central/io.opencensus/opencensus-api