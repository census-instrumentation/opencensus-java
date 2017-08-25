# OpenCensus Stackdriver Trace Exporter
[![Build Status][travis-image]][travis-url] [![Build status][appveyor-image]][appveyor-url] [![Maven Central][maven-image]][maven-url]

The *OpenCensus Stackdriver Trace Exporter* is a trace exporter that exports data to 
[Stackdriver Trace][stackdriver-url].

## Prerequisites

To use this exporter, you must have an application that you'd like to trace. The app can be on 
Google Cloud Platform, on-premise, or another cloud platform.

In order to be able to push your traces to [Stackdriver Trace][stackdriver-url], you must:

1. [Create a Cloud project](https://support.google.com/cloud/answer/6251787?hl=en).
2. [Enable billing](https://support.google.com/cloud/answer/6288653#new-billing).
3. [Enable the Stackdriver Trace API](https://console.cloud.google.com/apis/api/cloudtrace.googleapis.com/overview).

These steps enable the API but don't require that your app is hosted on Google Cloud Platform.

## Hello "Stackdriver Trace"

### Add the dependencies to your project.

For Maven add to your `pom.xml`:
```xml
<dependencies>
  <dependency>
    <groupId>io.netty</groupId>
    <artifactId>netty-tcnative-boringssl-static</artifactId>
    <version>2.0.3.Final</version>
  </dependency>
  <dependency>
    <groupId>io.opencensus</groupId>
    <artifactId>opencensus-exporter-trace-stackdriver</artifactId>
    <version>0.6.0</version>
  </dependency>
</dependencies>
```

For Gradle add to your dependencies:
```gradle
compile 'io.netty:netty-tcnative-boringssl-static:2.0.3.Final'
compile 'io.opencensus:opencensus-exporter-trace-stackdriver:0.6.0'

```

### Register the exporter
```java
public class MyMainClass {
  public static void main(String[] args) throws Exception {
    StackdriverExporter.createAndRegister("MyStackdriverProjectId");
    // ...
  }
}
```

## FAQ
The current Stackdriver Trace exporter is implemented using the [v1 API][stackdriver-v1-api-url]
which is not fully compatible with the OpenCensus data model. Trace events like Annotations and 
NetworkEvents will be dropped. Soon a v2 API will be available.

[travis-image]: https://travis-ci.org/census-instrumentation/opencensus-java.svg?branch=master
[travis-url]: https://travis-ci.org/census-instrumentation/opencensus-java
[appveyor-image]: https://ci.appveyor.com/api/projects/status/hxthmpkxar4jq4be/branch/master?svg=true
[appveyor-url]: https://ci.appveyor.com/project/instrumentationjavateam/opencensus-java/branch/master
[maven-image]: https://maven-badges.herokuapp.com/maven-central/io.opencensus/opencensus-exporter-trace-stackdriver/badge.svg
[maven-url]: https://maven-badges.herokuapp.com/maven-central/io.opencensus/opencensus-exporter-trace-stackdriver
[stackdriver-url]: https://cloud.google.com/trace/
[stackdriver-v1-api-url]: https://cloud.google.com/trace/docs/reference/v1/rpc/google.devtools.cloudtrace.v1#google.devtools.cloudtrace.v1.TraceSpan