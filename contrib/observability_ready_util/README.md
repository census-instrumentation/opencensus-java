# OpenCensus Observability Ready Util for Java

The *OpenCensus Observability Ready Util for Java* provides an easy function to setup OpenCensus
related code to debug the issues.

By default, it will setup following things:
* All basic RPC views are enabled.
* Set probabilistic sampling rate to 1 in 10,000.
* Create and register OpenCensus Trace agent exporter.
* Create and register OpenCensus Metrics agent exporter.

## Quickstart

### Prerequisites

> TODO

### Add the dependencies to your project

For Maven add to your `pom.xml`:
```xml
<dependencies>
  <dependency>
    <groupId>io.opencensus</groupId>
    <artifactId>opencensus-contrib-observability-ready-util</artifactId>
    <version>0.25.0</version>
  </dependency>
</dependencies>
```

For Gradle add to your dependencies:
```groovy
compile 'io.opencensus:opencensus-contrib-observability-ready-util:0.25.0'
```

### And the following code:

> TODO

