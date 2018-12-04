# OpenCensus Java OC-Agent Metrics Exporter

The *OpenCensus Java OC-Agent Metrics Exporter* is the Java implementation of the OpenCensus Agent
(OC-Agent) Metrics Exporter.

## Quickstart

### Add the dependencies to your project

For Maven add to your `pom.xml`:
```xml
<dependencies>
  <dependency>
    <groupId>io.opencensus</groupId>
    <artifactId>opencensus-api</artifactId>
    <version>0.19.0</version>
  </dependency>
  <dependency>
    <groupId>io.opencensus</groupId>
    <artifactId>opencensus-exporter-metrics-ocagent</artifactId>
    <version>0.19.0</version>
  </dependency>
  <dependency>
    <groupId>io.opencensus</groupId>
    <artifactId>opencensus-impl</artifactId>
    <version>0.19.0</version>
    <scope>runtime</scope>
  </dependency>
</dependencies>
```

For Gradle add to your dependencies:
```gradle
compile 'io.opencensus:opencensus-api:0.19.0'
compile 'io.opencensus:opencensus-exporter-metrics-ocagent:0.19.0'
runtime 'io.opencensus:opencensus-impl:0.19.0'
```

### Register the exporter

```java
import io.opencensus.exporter.metrics.ocagent.OcAgentMetricsExporter;

public class MyMainClass {
  public static void main(String[] args) throws Exception {
    OcAgentMetricsExporter.createAndRegister();
    // ...
  }
}
```
