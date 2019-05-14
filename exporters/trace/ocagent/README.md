# OpenCensus Java OC-Agent Trace Exporter

The *OpenCensus Java OC-Agent Trace Exporter* is the Java implementation of the OpenCensus Agent
(OC-Agent) Trace Exporter.

## Quickstart

### Add the dependencies to your project

For Maven add to your `pom.xml`:
```xml
<dependencies>
  <dependency>
    <groupId>io.opencensus</groupId>
    <artifactId>opencensus-api</artifactId>
    <version>0.22.0</version>
  </dependency>
  <dependency>
    <groupId>io.opencensus</groupId>
    <artifactId>opencensus-exporter-trace-ocagent</artifactId>
    <version>0.22.0</version>
  </dependency>
  <dependency>
    <groupId>io.opencensus</groupId>
    <artifactId>opencensus-impl</artifactId>
    <version>0.22.0</version>
    <scope>runtime</scope>
  </dependency>
</dependencies>
```

For Gradle add to your dependencies:
```groovy
compile 'io.opencensus:opencensus-api:0.22.0'
compile 'io.opencensus:opencensus-exporter-trace-ocagent:0.22.0'
runtime 'io.opencensus:opencensus-impl:0.22.0'
```

### Register the exporter

```java
public class MyMainClass {
  public static void main(String[] args) throws Exception {
    OcAgentTraceExporter.createAndRegister();
    // ...
  }
}
```
