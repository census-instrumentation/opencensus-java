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
    <version>0.17.0</version>
  </dependency>
</dependencies>
```

For Gradle add to your dependencies:
```gradle
compile 'io.opencensus:opencensus-dropwizard:0.17.0'
```

## Translation to OpenCensus Metrics

This section describes how each of the DropWizard metrics translate into OpenCensus metrics.

### DropWizard Counters

Given a DropWizard Counter with name `cache_evictions`, the following values are reported:

* name: cache_evictions_count
* description: Collected from Dropwizard (metric=<metric_name>, type=<class_name>)
(ex: Collected from Dropwizard (metric=cache_evictions, type=com.codahale.metrics.Counter))
* type: GAUGE_INT64
* unit: 1

Note: OpenCensus's CUMULATIVE_INT64 type represent monotonically increasing values. Since
DropWizard Counter goes up/down, it make sense to report them as OpenCensus GAUGE_INT64.

### DropWizard Gauges

Given a DropWizard Gauge with name `line_requests`, the following values are reported:

* name: line_requests_value
* description: Collected from Dropwizard (metric=<metric_name>, type=<class_name>)
* type: GAUGE_INT64 or GAUGE_DOUBLE
* unit: 1

### DropWizard Meters

Given a DropWizard Meter with name `get_requests`, the following values are reported:

* name: get_requests_count
* description: Collected from Dropwizard (metric=<metric_name>, type=<class_name>)
* type: GAUGE_INT64
* unit: 1

rate metrics:
* name: get_requests_rate
* description: Collected from Dropwizard (metric=<metric_name>, type=<class_name>)
* type: GAUGE_DOUBLE
* unit: "events/second"
* labelkeys: rate (with 4 TimeSeries, one corresponding to each rate: mean_rate, m1_rate, 
m5_rate m15_rate)

### DropWizard Histograms

Given a DropWizard Histogram with name `results`, the following values are reported:

* name: results_count
* description: Collected from Dropwizard (metric=<metric_name>, type=<class_name>)
* type: SUMMARY
* unit: 1

### DropWizard Timers

Given a DropWizard Timer with name `requests`, the following values are reported:
* name: requests_count
* description: Collected from Dropwizard (metric=<metric_name>, type=<class_name>)
* type: SUMMARY
* unit: 1
