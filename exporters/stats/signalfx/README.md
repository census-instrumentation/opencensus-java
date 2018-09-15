# OpenCensus SignalFx Stats Exporter

The _OpenCensus SignalFx Stats Exporter_ is a stats exporter that
exports data to [SignalFx](https://signalfx.com), a real-time monitoring
solution for cloud and distributed applications. SignalFx ingests that
data and offers various visualizations on charts, dashboards and service
maps, as well as real-time anomaly detection.

## Quickstart

### Prerequisites

To use this exporter, you must have a [SignalFx](https://signalfx.com)
account and corresponding [data ingest
token](https://docs.signalfx.com/en/latest/admin-guide/tokens.html).

#### Java versions

This exporter requires Java 7 or above.

### Add the dependencies to your project

For Maven add to your `pom.xml`:

```xml
<dependencies>
  <dependency>
    <groupId>io.opencensus</groupId>
    <artifactId>opencensus-api</artifactId>
    <version>0.16.0</version>
  </dependency>
  <dependency>
    <groupId>io.opencensus</groupId>
    <artifactId>opencensus-exporter-stats-signalfx</artifactId>
    <version>0.16.0</version>
  </dependency>
  <dependency>
    <groupId>io.opencensus</groupId>
    <artifactId>opencensus-impl</artifactId>
    <version>0.16.0</version>
    <scope>runtime</scope>
  </dependency>
</dependencies>
```

For Gradle add to your dependencies:

```
compile 'io.opencensus:opencensus-api:0.16.0'
compile 'io.opencensus:opencensus-exporter-stats-signalfx:0.16.0'
runtime 'io.opencensus:opencensus-impl:0.16.0'
```

### Register the exporter

```java
public class MyMainClass {
  public static void main(String[] args) {
    // SignalFx token is read from Java system properties.
    // Stats will be reported every second by default.
    SignalFxStatsExporter.create(SignalFxStatsConfiguration.builder().build());
  }
}
```

If you want to pass in the token yourself, or set a different reporting
interval, use:

```java
// Use token "your_signalfx_token" and report every 5 seconds.
SignalFxStatsExporter.create(
    SignalFxStatsConfiguration.builder()
        .setToken("your_signalfx_token")
        .setExportInterval(Duration.create(5, 0))
        .build());
```
