## Unreleased
- Add APIs to register gRPC client and server views separately.
- Add API MeasureMap.withAttachments() for recording exemplars.

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
