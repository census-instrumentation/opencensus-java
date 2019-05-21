# OpenCensus - A stats collection and distributed tracing framework
[![Gitter chat][gitter-image]][gitter-url]
[![Maven Central][maven-image]][maven-url]
[![Javadocs][javadoc-image]][javadoc-url]
[![Build Status][travis-image]][travis-url]
[![Windows Build Status][appveyor-image]][appveyor-url]
[![Coverage Status][codecov-image]][codecov-url]


OpenCensus is a toolkit for collecting application performance and behavior data. It currently
includes 3 apis: stats, tracing and tags.

The library is in [Beta](#versioning) stage and APIs are expected to be mostly stable. The 
library is expected to move to [GA](#versioning) stage after v1.0.0 major release.

Please join [gitter](https://gitter.im/census-instrumentation/Lobby) for help or feedback on this
project.

## OpenCensus Quickstart for Libraries

Integrating OpenCensus with a new library means recording stats or traces and propagating context.
For application integration please see [Quickstart for Applications](https://github.com/census-instrumentation/opencensus-java#quickstart-for-applications).

The full quick start example can also be found on the [OpenCensus website](https://opencensus.io/java/index.html).

### Add the dependencies to your project

For Maven add to your `pom.xml`:
```xml
<dependencies>
  <dependency>
    <groupId>io.opencensus</groupId>
    <artifactId>opencensus-api</artifactId>
    <version>0.22.1</version>
  </dependency>
</dependencies>
```

For Gradle add to your dependencies:
```groovy
compile 'io.opencensus:opencensus-api:0.22.1'
```

### Hello "OpenCensus" trace events

Here's an example of creating a Span and record some trace annotations. Notice that recording the
annotations is possible because we propagate scope. 3rd parties libraries like SLF4J can integrate
the same way.

```java
import io.opencensus.common.Scope;
import io.opencensus.trace.Tracer;
import io.opencensus.trace.Tracing;
import io.opencensus.trace.samplers.Samplers;

public final class MyClassWithTracing {
  private static final Tracer tracer = Tracing.getTracer();

  public static void doWork() {
    // Create a child Span of the current Span. Always record events for this span and force it to
    // be sampled. This makes it easier to try out the example, but unless you have a clear use
    // case, you don't need to explicitly set record events or sampler.
    try (Scope ss =
        tracer
            .spanBuilder("MyChildWorkSpan")
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
import io.opencensus.common.Scope;
import io.opencensus.stats.Aggregation;
import io.opencensus.stats.BucketBoundaries;
import io.opencensus.stats.Measure.MeasureLong;
import io.opencensus.stats.Stats;
import io.opencensus.stats.StatsRecorder;
import io.opencensus.stats.View;
import io.opencensus.stats.ViewData;
import io.opencensus.stats.ViewManager;
import io.opencensus.tags.TagKey;
import io.opencensus.tags.TagValue;
import io.opencensus.tags.Tagger;
import io.opencensus.tags.Tags;
import java.util.Arrays;
import java.util.Collections;

public final class MyClassWithStats {
  private static final Tagger tagger = Tags.getTagger();
  private static final ViewManager viewManager = Stats.getViewManager();
  private static final StatsRecorder statsRecorder = Stats.getStatsRecorder();

  // frontendKey allows us to break down the recorded data
  private static final TagKey FRONTEND_KEY = TagKey.create("myorg_keys_frontend");

  // videoSize will measure the size of processed videos.
  private static final MeasureLong VIDEO_SIZE =
      MeasureLong.create("my.org/measure/video_size", "size of processed videos", "By");

  // Create view to see the processed video size distribution broken down by frontend.
  // The view has bucket boundaries (0, 256, 65536) that will group measure values into
  // histogram buckets.
  private static final View.Name VIDEO_SIZE_VIEW_NAME = View.Name.create("my.org/views/video_size");
  private static final View VIDEO_SIZE_VIEW =
      View.create(
          VIDEO_SIZE_VIEW_NAME,
          "processed video size over time",
          VIDEO_SIZE,
          Aggregation.Distribution.create(
              BucketBoundaries.create(Arrays.asList(0.0, 256.0, 65536.0))),
          Collections.singletonList(FRONTEND_KEY));

  public static void initialize() {
    // ...
    viewManager.registerView(VIDEO_SIZE_VIEW);
  }

  public static void processVideo() {
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

  public static void printStats() {
    ViewData viewData = viewManager.getView(VIDEO_SIZE_VIEW_NAME);
    System.out.println(
        String.format("Recorded stats for %s:\n %s", VIDEO_SIZE_VIEW_NAME.asString(), viewData));
  }
}
```

## OpenCensus Quickstart for Applications

Besides recording tracing/stats events the application also need to link the implementation,
setup exporters, and debugging [Z-Pages](https://github.com/census-instrumentation/opencensus-java/tree/master/contrib/zpages).

### Add the dependencies to your project

For Maven add to your `pom.xml`:
```xml
<dependencies>
  <dependency>
    <groupId>io.opencensus</groupId>
    <artifactId>opencensus-api</artifactId>
    <version>0.22.1</version>
  </dependency>
  <dependency>
    <groupId>io.opencensus</groupId>
    <artifactId>opencensus-impl</artifactId>
    <version>0.22.1</version>
    <scope>runtime</scope>
  </dependency>
</dependencies>
```

For Gradle add to your dependencies:
```groovy
compile 'io.opencensus:opencensus-api:0.22.1'
runtime 'io.opencensus:opencensus-impl:0.22.1'
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

## Versioning
  
This library follows [Semantic Versioning][semver].
  
**GA**: Libraries defined at a GA quality level are stable, and will not introduce 
backwards-incompatible changes in any minor or patch releases. We will address issues and requests 
with the highest priority. If we were to make a backwards-incompatible changes on an API, we will 
first mark the existing API as deprecated and keep it for 18 months before removing it.
  
**Beta**: Libraries defined at a Beta quality level are expected to be mostly stable and we're 
working towards their release candidate. We will address issues and requests with a higher priority.
There may be backwards incompatible changes in a minor version release, though not in a patch 
release. If an element is part of an API that is only meant to be used by exporters or other 
opencensus libraries, then there is no deprecation period. Otherwise, we will deprecate it for 18 
months before removing it, if possible.

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
[semver]: http://semver.org/
[TraceExporterInstana]: https://github.com/census-instrumentation/opencensus-java/tree/master/exporters/trace/instana#quickstart
[TraceExporterJaeger]: https://github.com/census-instrumentation/opencensus-java/tree/master/exporters/trace/jaeger#quickstart
[TraceExporterLogging]: https://github.com/census-instrumentation/opencensus-java/tree/master/exporters/trace/logging#quickstart
[TraceExporterStackdriver]: https://github.com/census-instrumentation/opencensus-java/tree/master/exporters/trace/stackdriver#quickstart
[TraceExporterZipkin]: https://github.com/census-instrumentation/opencensus-java/tree/master/exporters/trace/zipkin#quickstart
[StatsExporterStackdriver]: https://github.com/census-instrumentation/opencensus-java/tree/master/exporters/stats/stackdriver#quickstart
[StatsExporterSignalFx]: https://github.com/census-instrumentation/opencensus-java/tree/master/exporters/stats/signalfx#quickstart
[StatsExporterPrometheus]: https://github.com/census-instrumentation/opencensus-java/tree/master/exporters/stats/prometheus#quickstart
