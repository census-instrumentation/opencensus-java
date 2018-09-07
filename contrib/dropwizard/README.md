# OpenCensus DropWizard Util for Java

The *OpenCensus DropWizard Util for Java* provides an easy way to send metrics from your DropWizard
project to OpenCensus.

## Quickstart

### Add the dependencies to your project

For Maven add to your `pom.xml`:
```xml
<dependencies>
  <dependency>
    <groupId>io.opencensus</groupId>
    <artifactId>opencensus-contrib-dropwizard</artifactId>
    <version>0.16.0</version>
  </dependency>
</dependencies>
```

For Gradle add to your dependencies:
```gradle
compile 'io.opencensus:opencensus-dropwizard:0.16.0'
```

## Translation to OpenCensus Metrics

This section describes how each of the DropWizard metrics translate into OpenCensus metrics.

### DropWizard Gauges

Given a DropWizard Gauge with name `line_requests`, the following values are reported:

* name: line_requests_value
* description: DropWizard Metric=Gauge, Data=value
* type: GAUGE_INT64 or GAUGE_DOUBLE
* unit: 1

### DropWizard Counters

Given a DropWizard Counter with name `cache_evictions`, the following values are reported:

* name: cache_evictions_count
* description: DropWizard Metric=Counter, Data=count
* type: GAUGE_INT64
* unit: 1

Note: OpenCensus's CUMULATIVE_INT64 type represent monotonically increasing values. Since
DropWizard Counter goes up/down, it make sense to report them as OpenCensus GAUGE_INT64.

### DropWizard Meters

Given a DropWizard Meter with name `get_requests`, the following values are reported:

* name: get_requests_count
* description: DropWizard Metric=Meter, Data=count
* type: GAUGE_INT64
* unit: 1

rate metrics:
* name: get_requests_rate
* description: DropWizard Metric=Meter, Data=rate
* type: GAUGE_DOUBLE
* unit: "events/second"
* labelkeys: rate
* labelvalues: mean_rate, m1_rate, m5_rate_ m15_rate

### DropWizard Histograms

Given a DropWizard Histogram with name `results`, the following values are reported:

* name: results_count
* description: DropWizard Metric=Snapshot, Data=count
* type: CUMULATIVE_INT64
* unit: 1

quantile metrics:
* name: results_quantile
* description: DropWizard Metric=Meter, Data=quantile
* type: GAUGE_DOUBLE
* unit: 1
* labelkeys: quantile
* labelvalues: PCT_50, PCT_75, PCT_95 PCT_98, PCT_99 PCT_999

### DropWizard Timers

Given a DropWizard Timer with name `requests`, the following values are reported:
* name: requests_count
* description: DropWizard Metric=Snapshot, Data=count
* type: CUMULATIVE_INT64
* unit: 1

quantile metrics:
* name: results_quantile
* description: DropWizard Metric=Meter, Data=quantile
* type: GAUGE_DOUBLE
* unit: 1
* labelkeys: quantile
* labelvalues: PCT_50, PCT_75, PCT_95 PCT_98, PCT_99 PCT_999
