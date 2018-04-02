# OpenCensus - A stats collection and distributed tracing framework
[![Gitter chat][gitter-image]][gitter-url]
[![Maven Central][maven-image]][maven-url]
[![Javadocs][javadoc-image]][javadoc-url]
[![Build Status][travis-image]][travis-url]
[![Windows Build Status][appveyor-image]][appveyor-url]
[![Coverage Status][codecov-image]][codecov-url]


OpenCensus is a toolkit for collecting application performance and behavior data. It currently
includes 3 apis: stats, tracing and tags.

The library is in alpha stage and the API is subject to change.

Please join [gitter](https://gitter.im/census-instrumentation/Lobby) for help or feedback on this
project.

## OpenCensus Quickstart

Integrating OpenCensus with a new library means recording stats or traces and propagating context.

TODO: add a link to Java quick start documentation on opencensus.io once it's ready.

### Add the dependencies to your project

For Maven add to your `pom.xml`:
```xml
<dependencies>
  <dependency>
    <groupId>io.opencensus</groupId>
    <artifactId>opencensus-api</artifactId>
    <version>0.12.2</version>
  </dependency>
</dependencies>
```

For Gradle add to your dependencies:
```gradle
compile 'io.opencensus:opencensus-api:0.12.2'
```

### Hello "OpenCensus" trace events

Here's an example of creating a Span and record some trace annotations. Notice that recording the
annotations is possible because we propagate scope. 3rd parties libraries like SLF4J can integrate
the same way.

```java
public final class MyClassWithTracing {
  private static final Tracer tracer = Tracing.getTracer();

  public static void doWork() {
    // Create a child Span of the current Span. Always record events for this span and force it to 
    // be sampled.
    try (Scope ss = 
         tracer.spanBuilder("MyChildWorkSpan")
           .setRecordEvents(true)
           .setSampler(Samplers.alwaysSample())
           .startScopedSpan()) {
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

Here's an example on
 * defining TagKey, Measure and View,
 * registering a view,
 * putting TagKey and TagValue into a scoped TagContext,
 * recording stats against current TagContext,
 * getting ViewData.

 
For the complete example, see
[here](https://github.com/census-instrumentation/opencensus-java/blob/master/examples/src/main/java/io/opencensus/examples/helloworld/QuickStart.java).

```java
public final class QuickStart {
  private static final Tagger tagger = Tags.getTagger();
  private static final ViewManager viewManager = Stats.getViewManager();
  private static final StatsRecorder statsRecorder = Stats.getStatsRecorder();

  // frontendKey allows us to break down the recorded data
  private static final TagKey FRONTEND_KEY = TagKey.create("myorg_keys_frontend");

  // videoSize will measure the size of processed videos.
  private static final MeasureLong VIDEO_SIZE = MeasureLong.create(
      "my.org/measure/video_size", "size of processed videos", "By");

  // Create view to see the processed video size distribution broken down by frontend.
  // The view has bucket boundaries (0, 256, 65536) that will group measure values into
  // histogram buckets.
  private static final View.Name VIDEO_SIZE_VIEW_NAME = View.Name.create("my.org/views/video_size");
  private static final View VIDEO_SIZE_VIEW = View.create(
      VIDEO_SIZE_VIEW_NAME,
      "processed video size over time",
      VIDEO_SIZE,
      Aggregation.Distribution.create(BucketBoundaries.create(Arrays.asList(0.0, 256.0, 65536.0))),
      Collections.singletonList(FRONTEND_KEY),
      Cumulative.create());

  private static void initialize() {
    // ...
    viewManager.registerView(VIDEO_SIZE_VIEW);
  }

  private static void processVideo() {
    try (Scope scopedTags =
           tagger
             .currentBuilder()
             .put(FRONTEND_KEY, TagValue.create("mobile-ios9.3.5"))
             .buildScoped()) {
      // Processing video.
      // ...

      // Record the processed video size.
      statsRecorder.newMeasureMap().put(VIDEO_SIZE, 25648).record();
    }
  }

  private static void printStats() {
    ViewData viewData = viewManager.getView(VIDEO_SIZE_VIEW_NAME);
    System.out.println(
      String.format("Recorded stats for %s:\n %s", VIDEO_SIZE_VIEW_NAME.asString(), viewData));
  }
}
```

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
    <version>0.12.2</version>
  </dependency>
  <dependency>
    <groupId>io.opencensus</groupId>
    <artifactId>opencensus-impl</artifactId>
    <version>0.12.2</version>
    <scope>runtime</scope>
  </dependency>
</dependencies>
```

For Gradle add to your dependencies:
```gradle
compile 'io.opencensus:opencensus-api:0.12.2'
runtime 'io.opencensus:opencensus-impl:0.12.2'
```

### How to setup exporters?

#### Trace exporters
* [Instana][TraceExporterInstana]
* [Jaeger][TraceExporterJaeger]
* [Logging][TraceExporterLogging]
* [Stackdriver][TraceExporterStackdriver]
* [Zipkin][TraceExporterZipkin]

#### Stats exporters
* [Stackdriver][StatsExporterStackdriver]
* [SignalFx][StatsExporterSignalFx]
* [Prometheus][StatsExporterPrometheus]

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
[gitter-image]: https://badges.gitter.im/census-instrumentation/lobby.svg
[gitter-url]: https://gitter.im/census-instrumentation/lobby?utm_source=badge&utm_medium=badge&utm_campaign=pr-badge&utm_content=badge
[codecov-image]: https://codecov.io/gh/census-instrumentation/opencensus-java/branch/master/graph/badge.svg
[codecov-url]: https://codecov.io/gh/census-instrumentation/opencensus-java/branch/master/
[TraceExporterInstana]: https://github.com/census-instrumentation/opencensus-java/tree/master/exporters/trace/instana#quickstart
[TraceExporterJaeger]: https://github.com/census-instrumentation/opencensus-java/tree/master/exporters/trace/jaeger#quickstart
[TraceExporterLogging]: https://github.com/census-instrumentation/opencensus-java/tree/master/exporters/trace/logging#quickstart
[TraceExporterStackdriver]: https://github.com/census-instrumentation/opencensus-java/tree/master/exporters/trace/stackdriver#quickstart
[TraceExporterZipkin]: https://github.com/census-instrumentation/opencensus-java/tree/master/exporters/trace/zipkin#quickstart
[StatsExporterStackdriver]: https://github.com/census-instrumentation/opencensus-java/tree/master/exporters/stats/stackdriver#quickstart
[StatsExporterSignalFx]: https://github.com/census-instrumentation/opencensus-java/tree/master/exporters/stats/signalfx#quickstart
[StatsExporterPrometheus]: https://github.com/census-instrumentation/opencensus-java/tree/master/exporters/stats/prometheus#quickstart
