# OpenCensus Prometheus Stats Exporter

The *OpenCensus Prometheus Stats Exporter* is a stats exporter that exports data to 
Prometheus. [Prometheus](https://prometheus.io/) is an open-source systems monitoring and alerting 
toolkit originally built at [SoundCloud](https://soundcloud.com/).

## Quickstart

### Prerequisites

To use this exporter, you need to install, configure and start Prometheus first. Follow the 
instructions [here](https://prometheus.io/docs/introduction/first_steps/).

### Hello "Prometheus Stats"

#### Add the dependencies to your project

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
    <artifactId>opencensus-exporter-stats-prometheus</artifactId>
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
compile 'io.opencensus:opencensus-exporter-stats-prometheus:0.22.1'
runtime 'io.opencensus:opencensus-impl:0.22.1'
```

#### Register the exporter
 
```java
public class MyMainClass {
  public static void main(String[] args) {
    // Creates a PrometheusStatsCollector and registers it to the default Prometheus registry.
    PrometheusStatsCollector.createAndRegister();
    
    // Uses a simple Prometheus HTTPServer to export metrics. 
    // You can use a Prometheus PushGateway instead, though that's discouraged by Prometheus:
    // https://prometheus.io/docs/practices/pushing/#should-i-be-using-the-pushgateway.
    io.prometheus.client.exporter.HTTPServer server = 
      new HTTPServer(/*host*/ "localhost", /*port*/  9091, /*daemon*/ true);
    
    // Your code here.
    // ...
  }
}
```

In this example, you should be able to see all the OpenCensus Prometheus metrics by visiting 
localhost:9091/metrics. Every time when you visit localhost:9091/metrics, the metrics will be 
collected from OpenCensus library and refreshed.

#### Exporting

After collecting stats from OpenCensus, there are multiple options for exporting them. 
See [Exporting via HTTP](https://github.com/prometheus/client_java#http), [Exporting to a Pushgateway](https://github.com/prometheus/client_java#exporting-to-a-pushgateway)
and [Bridges](https://github.com/prometheus/client_java#bridges).

#### Java Versions

Java 7 or above is required for using this exporter.

## FAQ
