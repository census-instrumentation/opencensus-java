# OpenCensus DropWizard Util for Java

The *OpenCensus DropWizard Util for Java* provides an easy way to translate Dropwizard metrics to
OpenCensus.

## Quickstart

### Prerequisites

Assuming, you already have both the OpenCensus and Dropwizard client libraries setup and working
inside your application.

### Add the dependencies to your project

For Maven add to your `pom.xml`:
```xml
<dependencies>
  <dependency>
    <groupId>io.opencensus</groupId>
    <artifactId>opencensus-contrib-dropwizard5</artifactId>
    <version>0.31.0</version>
  </dependency>
</dependencies>
```

For Gradle add to your dependencies:
```groovy
compile 'io.opencensus:opencensus-contrib-dropwizard5:0.31.0'
```

### And the following code:

```java
import java.util.Collections;

public class YourClass {
  // Create registry for Dropwizard metrics.
  static final io.dropwizard.metrics5.MetricRegistry codahaleRegistry =
    new io.dropwizard.metrics5.MetricRegistry();

  // Create a Dropwizard counter.
  Map<String, String> tags = new HashMap<>();
  tags.put("tag1", "value1");
  tags.put("tag2", "value2");
  static final io.dropwizard.metrics5.Counter requests =
    codahaleRegistry.counter(new MetricName("requests", tags));

  public static void main(String[] args) {

    // Increment the requests.
    requests.inc();

    // Hook the Dropwizard registry into the OpenCensus registry
    // via the DropWizardMetrics metric producer.
    io.opencensus.metrics.Metrics.getExportComponent().getMetricProducerManager().add(
          new io.opencensus.contrib.dropwizard.DropWizardMetrics(
            Collections.singletonList(codahaleRegistry)));

  }
}
```

## Translation to OpenCensus Metrics

This section describes how each of the DropWizard metrics translate into OpenCensus metrics.

### DropWizard Counters

Given a DropWizard Counter with name `cache_evictions`, the following values are reported:

* name: dropwizard5_<initial_metric_name>_<initial_type> (ex: codahale_cache_evictions_counter)
* description: Collected from Dropwizard (metric=<metric_name>, type=<class_name>)
(ex: Collected from Dropwizard (metric=cache_evictions, type=io.dropwizard.metrics5.Counter))
* type: GAUGE_INT64
* unit: 1
* labels: metrics tags are converted to label keys/values

Note: OpenCensus's CUMULATIVE_INT64 type represent monotonically increasing values. Since
DropWizard Counter goes up/down, it make sense to report them as OpenCensus GAUGE_INT64.

### DropWizard Gauges

Given a DropWizard Gauge with name `line_requests`, the following values are reported:

* name: dropwizard5_<initial_metric_name>_<initial_type> (ex: codahale_line_requests_gauge)
* description: Collected from Dropwizard (metric=<metric_name>, type=<class_name>)
* type: GAUGE_INT64 or GAUGE_DOUBLE
* unit: 1
* labels: metrics tags are converted to label keys/values


Note: For simplicity, OpenCensus uses GAUGE_DOUBLE type for any Number and GAUGE_INT64
type for Boolean values.

### DropWizard Meters

Given a DropWizard Meter with name `get_requests`, the following values are reported:

* name: dropwizard5_<initial_metric_name>_<initial_type> (ex: codahale_get_requests_meter)
* description: Collected from Dropwizard (metric=<metric_name>, type=<class_name>)
* type: CUMULATIVE_INT64
* unit: 1
* labels: metrics tags are converted to label keys/values


### DropWizard Histograms

Given a DropWizard Histogram with name `results`, the following values are reported:

* name: dropwizard5_<initial_metric_name>_<initial_type> (ex: codahale_results_histogram)
* description: Collected from Dropwizard (metric=<metric_name>, type=<class_name>)
* type: SUMMARY
* unit: 1
* labels: metrics tags are converted to label keys/values


### DropWizard Timers

Given a DropWizard Timer with name `requests`, the following values are reported:
* name: dropwizard5_<initial_metric_name>_<initial_type> (ex: codahale_requests_timer)
* description: Collected from Dropwizard (metric=<metric_name>, type=<class_name>)
* type: SUMMARY
* unit: 
* labels: metrics tags are converted to label keys/values

