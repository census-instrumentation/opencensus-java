# OpenCensus Log4j 2 Log Correlation

The `opencensus-contrib-log-correlation-log4j2` artifact provides a
[Log4j 2](https://logging.apache.org/log4j/2.x/)
[`ContextDataInjector`](https://logging.apache.org/log4j/2.x/manual/extending.html#Custom_ContextDataInjector)
that automatically adds tracing data to the context of Log4j
[`LogEvent`](https://logging.apache.org/log4j/2.x/log4j-core/apidocs/org/apache/logging/log4j/core/LogEvent.html)s.
The class name is
`OpenCensusTraceContextDataInjector`. `OpenCensusTraceContextDataInjector` adds the current trace
ID, span ID, and sampling decision to each `LogEvent`, so that they can be accessed with
[`LogEvent.getContextData()`](https://logging.apache.org/log4j/2.x/log4j-core/apidocs/org/apache/logging/log4j/core/LogEvent.html#getContextData())
or included in a layout.

See
https://github.com/census-ecosystem/opencensus-experiments/tree/master/java/log_correlation/log4j2
for a demo that uses this library to correlate logs and traces in Stackdriver.

## Instructions

### Add the dependencies to your project

For Maven add to your `pom.xml`:
```xml
<dependencies>
  <dependency>
    <groupId>io.opencensus</groupId>
    <artifactId>opencensus-contrib-log-correlation-log4j2</artifactId>
    <version>0.16.1</version>
    <scope>runtime</scope>
  </dependency>
</dependencies>
```

For Gradle add to your dependencies:
```groovy
runtime 'io.opencensus:opencensus-contrib-log-correlation-log4j2:0.16.1'
```

### Configure the `OpenCensusTraceContextDataInjector`

#### Specify the `ContextDataInjector` override

Override Log4j's default `ContextDataInjector` by setting the system property
`log4j2.contextDataInjector` to the full name of the class,
`io.opencensus.contrib.logcorrelation.log4j2.OpenCensusTraceContextDataInjector`.

#### Choose when to add tracing data to log events

The following system property controls the decision to add tracing data from the current span to a
log event:

`io.opencensus.contrib.logcorrelation.log4j2.OpenCensusTraceContextDataInjector.spanSelection`

The allowed values are:

* `ALL_SPANS`: adds tracing data to all log events (default)

* `NO_SPANS`: disables the log correlation feature

* `SAMPLED_SPANS`: adds tracing data to log events when the current span is sampled

### Add the tracing data to log entries

`opencensus-contrib-log-correlation-log4j2` adds the following key-value pairs to the `LogEvent`
context:

* `traceId` - the lowercase base16 encoding of the current trace ID
* `spanId` - the lowercase base16 encoding of the current span ID
* `traceSampled` - the sampling decision of the current span ("true" or "false")

These values can be accessed from layouts with
[Context Map Lookup](http://logging.apache.org/log4j/2.x/manual/lookups.html#ContextMapLookup).  For
example, the trace ID can be accessed with `$${ctx:traceId}`.  The values can also be accessed with
the `X` conversion character in
[`PatternLayout`](http://logging.apache.org/log4j/2.x/manual/layouts.html#PatternLayout), for
example, `%X{traceId}`.

See an example Log4j configuration file in the demo:
https://github.com/census-ecosystem/opencensus-experiments/tree/master/java/log_correlation/log4j2/src/main/resources/log4j2.xml

### Java Versions

Java 6 or above is required for using this artifact.
