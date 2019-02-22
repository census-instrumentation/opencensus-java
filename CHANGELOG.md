## Unreleased
- Add OpenCensus Java OC-Agent Trace Exporter.
- Add OpenCensus Java OC-Agent Metrics Exporter.
- Add config option for Http-Servlet.
- Add config option for Jetty Http Client.
- Modified default value to false for publicEndpoint property in Http-Servlet.
- Add a generic `AttachmentValue` class to support `Exemplar`.
- Add Elasticsearch Trace Exporter.
- Add `metrics.data` package to hold common classes shared between stats and metrics.
- Refactor `Exemplar` and `AttachmentValue` to be under `metrics.data`. Note that this is a breaking change
if you're using the `Exemplar` classes or APIs in the previous releases.
- Add `TagMetadata` that defines the properties associated with a `Tag`.

# 0.19.0 - 2019-01-28
- Add an artifact `opencensus-contrib-http-jetty-client` for instrumenting jetty http client. Add extractor for Jetty Client.
- Add an artifact `opencensus-contrib-http-servlets` for instrumenting http servlets. Add extractor for Http Servlets.
- Add support generic http server handler.
- Add support for generic http client handler.
- Add ability to filter metrics collected from Dropwizard registry.
- Add an util artifact opencensus-contrib-dropwizard5 to translate Dropwizard metrics5 to OpenCensus.
- Add metrics util package to be shared by all metrics exporters.
- Add Datadog Trace Exporter.

## 0.18.0 - 2018-11-27
- Set the
  [`trace_sampled` field](https://github.com/googleapis/googleapis/blob/8027f17420d5a323c7dfef1ae0e57d82f3b97430/google/logging/v2/log_entry.proto#L143-L149) in the Stackdriver `LogEntry` protocol buffer in `opencensus-contrib-log-correlation-stackdriver`.
- Add support for w3c/distributed-tracing propagation format.
- Add gRPC measures and views for real-time metrics in streaming RPCs.
- Add Summary Metric support for Stackdriver exporter.
- Reduce CPU usage for low qps applications.

## 0.17.0 - 2018-11-02
- Add `AttributeValueDouble` to `AttributeValue`.
- Add `createWithSender` to `JaegerTraceExporter` to allow use of `HttpSender`
  with extra configurations.
- Add an API `Functions.returnToString()`.
- Migrate to new Stackdriver Kubernetes monitored resource. This could be a breaking change
  if you are using `gke_container` resources. For more info,
  https://cloud.google.com/monitoring/kubernetes-engine/migration#incompatible
- Add an util artifact `opencensus-contrib-dropwizard` to translate Dropwizard metrics to
  OpenCensus.
- Add Gauges (`DoubleGauge`, `LongGauge`, `DerivedDoubleGauge`, `DerivedLongGauge`) APIs.
- Update `opencensus-contrib-log-correlation-log4j2` and
  `opencensus-contrib-log-correlation-stackdriver` to match the
  [OpenCensus log correlation spec](https://github.com/census-instrumentation/opencensus-specs/blob/master/trace/LogCorrelation.md)
  and remove all `ExperimentalApi` annotations.
- The histogram bucket boundaries (`BucketBoundaries`) and values (`Count` and `Sum`) are no longer
  supported for negative values. The Record API drops the negative `value` and logs the warning.
  This could be a breaking change if you are recording negative value for any `measure`.
- Remove support for min/max in the stats Distribution to make it compatible with Metrics.

## 0.16.1 - 2018-09-18
- Fix ClassCastException in Log4j log correlation
  ([#1436](https://github.com/census-instrumentation/opencensus-java/issues/1436)).
- Allow users to report metrics for their registered domain (using custom prefix). This could be a
  breaking change if you have custom prefix without (registered) domain.

## 0.16.0 - 2018-09-14
- Add APIs to register gRPC client and server views separately.
- Add an API MeasureMap.putAttachment() for recording exemplars.
- Add Exemplar class and an API to get Exemplar list to DistributionData.
- Improve the styling of Rpcz, Statsz, Tracez, and Traceconfigz pages.
- Add an artifact `opencensus-contrib-exemplar-util` that has helper utilities
  on recording exemplars.
- Reduce the default limit on `Link`s per `Span` to 32 (was 128 before).
- Add Spring support for `@Traced` annotation and java.sql.PreparedStatements
  tracing.
- Allow custom prefix for Stackdriver metrics in `StackdriverStatsConfiguration`.
- Add support to handle the Tracestate in the SpanContext.
- Remove global synchronization from the get current stats state.
- Add get/from{Byte} methods on TraceOptions and deprecate get/from{Bytes}.
- Add an API to `StackdriverTraceConfiguration` to allow setting a
  `TraceServiceStub` instance to be used for export RPC calls.
- Add an experimental artifact, `opencensus-contrib-log-correlation-log4j2`, for
  adding tracing data to Log4j 2 LogEvents.

## 0.15.1 - 2018-08-28
- Improve propagation performance by avoiding doing string formatting when calling checkArgument.

## 0.15.0 - 2018-06-20
- Expose the factory methods of MonitoredResource.
- Add an experimental artifact, `opencensus-contrib-log-correlation-stackdriver`, for
  correlating traces and logs with Stackdriver Logging.

## 0.14.0 - 2018-06-04
- Adds Tracing.getExportComponent().shutdown() for use within application shutdown hooks.
- `Duration.create` now throws an `IllegalArgumentException` instead of
  returning a zero `Duration` when the arguments are invalid.
- `Timestamp.create` now throws an `IllegalArgumentException` instead of
  returning a zero `Timestamp` when the arguments are invalid.
- Remove namespace and help message prefix for Prometheus exporter. This could be
  a breaking change if you have Prometheus metrics from OpenCensus Prometheus exporter
  of previous versions, please point to the new metrics with no namespace instead.
- Add an util artifact `opencensus-contrib-appengine-standard-util` to interact with the AppEngine
  CloudTraceContext.
- Add support for Span kinds. (fix [#1054](https://github.com/census-instrumentation/opencensus-java/issues/1054)).
- Add client/server started_rpcs measures and views to RPC constants.

## 0.13.2 - 2018-05-08
- Map http attributes to Stackdriver format (fix [#1153](https://github.com/census-instrumentation/opencensus-java/issues/1153)).

## 0.13.1 - 2018-05-02
- Fix a typo on displaying Aggregation Type for a View on StatsZ page.
- Set bucket bounds as "le" labels for Prometheus Stats exporter.

## 0.13.0 - 2018-04-27
- Support building with Java 9.
- Add a QuickStart example.
- Remove extraneous dependencies from the Agent's `pom.xml`.
- Deprecate `Window` and `WindowData`.
- Add a configuration class to the Prometheus stats exporter.
- Fix build on platforms that are not supported by `netty-tcnative`.
- Add Jaeger trace exporter.
- Add a gRPC Hello World example.
- Remove usages of Guava collections in `opencensus-api`.
- Set unit "1" when the aggregation type is Count.
- Auto detect GCE and GKE Stackdriver MonitoredResources.
- Make Error Prone and FindBugs annotations `compileOnly` dependencies.
- Deprecate `Mean` and `MeanData`.
- Sort `TagKey`s in `View.create(...)`.
- Add utility class to expose default HTTP measures, tags and view, and register
  default views.
- Add new RPC measure and view constants, deprecate old ones.
- Makes the trace and span ID fields mandatory in binary format.
- Auto detect AWS EC2 resources.
- Add `Duration.toMillis()`.
- Make monitored resource utils a separate artifact `opencensus-contrib-monitored-resource-util`,
  so that it can be reused across exporters.
- Add `LastValue`, `LastValueDouble` and `LastValueLong`. Also support them in
  stats exporters and zpages. Please note that there is an API breaking change
  in methods `Aggregation.match()` and `AggregationData.match()`.

## 0.12.3 - 2018-04-13
- Substitute non-ascii characters in B3Format header key.

## 0.12.2 - 2018-02-26
- Upgrade disruptor to include the fix for SleepingWaitStrategy causing 100%
  CPU.

## 0.12.1 - 2018-02-26
- Fix performance issue where unused objects were referenced by the Disruptor.
- Fix synchonization issue in the use of the Disruptor.

## 0.12.0 - 2018-02-16
- Rename trace exporters that have inconsistent naming. Exporters with legacy
  names are deprecated.
- Fixed bug in CloudTraceFormat that made it impossible to use short span id's.
- Add `since` Javadoc tag to all APIs.
- Add a configuration class to create StackdriverTraceExporter.
- Add MessageEvent and deprecate NetworkEvent.
- Instana Trace Exporter.
- Prometheus Stats Exporter.
- Stats Zpages: RpcZ and StatsZ.
- Dependency updates.

## 0.11.1 - 2018-01-23
- Fixed bug that made it impossible to use short span id's (#950).

## 0.11.0 - 2018-01-19
- Add TextFormat API and two implementations (B3Format and CloudTraceFormat).
- Add helper class to configure and create StackdriverStatsExporter.
- Add helper methods in tracer to wrap Runnable and Callbacks and to run them.
- Increase trace exporting interval to 5s.
- Add helper class to register views.
- Make stackdriver stats exporter compatible with GAE Java7.
- Add SignalFX stats exporter.
- Add http propagation APIs.
- Dependency updates.

## 0.10.0 - 2017-12-04
- Add NoopRunningSpanStore and NoopSampledSpanStore.
- Change the message event to include (un)compressed sizes for Tracez Zpage.
- Use AppEngine compatible way to create threads.
- Add new factory methods that support setting custom Stackdriver
  MonitoredResource for Stackdriver Stats Exporter.
- Dependency updates.

## 0.9.1 - 2017-11-29
- Fix several implementation bugs in Stackdriver Stats Exporter (#830, #831,
  etc.).
- Update length limit for View.Name to 255 (previously it's 256).

## 0.9.0 - 2017-11-17
- Initial stats and tagging implementation for Java (`impl`) and Android
  (`impl-lite`). This implements all the stats and tagging APIs since v0.8.0.
- Deprecate Tags.setState and Stats.setState.
- Add a setStatus method in the Span.
- OpenCensus Stackdriver Stats Exporter.
- OpenCensus Stackdriver Trace Exporter is updated to use Stackdriver Trace V2
  APIs.
- Dependency updates.
